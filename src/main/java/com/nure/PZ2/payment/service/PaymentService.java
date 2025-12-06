package com.nure.PZ2.payment.service;

import com.nure.PZ2.payment.client.BookingServiceClient;
import com.nure.PZ2.payment.dto.*;
import com.nure.PZ2.payment.exception.PaymentNotFoundException;
import com.nure.PZ2.payment.exception.PaymentAlreadyExistsException;
import com.nure.PZ2.payment.model.Payment;
import com.nure.PZ2.payment.model.Payment.Amount;
import com.nure.PZ2.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final AtomicInteger idCounter = new AtomicInteger(5002);
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository,
                          BookingServiceClient bookingServiceClient) {
        this.paymentRepository = paymentRepository;
        this.bookingServiceClient = bookingServiceClient;
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));
        return convertToDTO(payment);
    }

    public PaymentDTO getPaymentByBookingId(String bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for booking " + bookingId + ". " +
                                "The booking may not have been paid yet."
                ));
        return convertToDTO(payment);
    }

    public List<PaymentDTO> getPaymentsByStatus(String status) {
        // Validate status
        if (!List.of("PENDING", "COMPLETED", "FAILED", "REFUNDED").contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid payment status: " + status + ". " +
                            "Allowed values: PENDING, COMPLETED, FAILED, REFUNDED"
            );
        }

        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO createPayment(CreatePaymentRequest request) {
        validatePaymentRequest(request);

        // Check for duplicate payment
        if (paymentRepository.existsCompletedPaymentForBooking(request.getBookingId())) {
            throw new PaymentAlreadyExistsException(
                    "Payment already completed for booking " + request.getBookingId() + ". " +
                            "Cannot create duplicate payment."
            );
        }

        // INTER-SERVICE CALL: Validate booking exists in Booking Service
        System.out.println("[IPC] Validating booking " + request.getBookingId() + " with Booking Service...");

        BookingInfoDTO booking = bookingServiceClient.getBooking(request.getBookingId());

        if (booking == null) {
            // Check if Booking Service is available
            if (!bookingServiceClient.isServiceHealthy()) {
                throw new IllegalArgumentException(
                        "Cannot validate booking: Booking Service is not available at " +
                                bookingServiceClient.getServiceUrl() + ". Please ensure the service is running."
                );
            }

            // Service is available but booking not found
            throw new IllegalArgumentException(
                    "Booking " + request.getBookingId() + " not found in Booking Service. " +
                            "Please verify the booking ID is correct."
            );
        }

        // Validate booking status
        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalArgumentException(
                    "Booking " + request.getBookingId() + " is not in valid state for payment. " +
                            "Current status: " + booking.getStatus() + ". " +
                            "Only 'PENDING' or 'CONFIRMED' bookings can be paid."
            );
        }

        // Validate payment amount matches booking total
        double amountDifference = Math.abs(request.getAmount().getValue() - booking.getTotalPrice().getValue());
        if (amountDifference > 0.01) {
            throw new IllegalArgumentException(
                    "Payment amount mismatch: Requested " + request.getAmount().getValue() + " " +
                            request.getAmount().getCurrency() + ", but booking total is " +
                            booking.getTotalPrice().getValue() + " " + booking.getTotalPrice().getCurrency() + ". " +
                            "Payment amount must exactly match booking total."
            );
        }

        // Validate currency matches
        if (!request.getAmount().getCurrency().equals(booking.getTotalPrice().getCurrency())) {
            throw new IllegalArgumentException(
                    "Currency mismatch: Payment currency is " + request.getAmount().getCurrency() +
                            ", but booking currency is " + booking.getTotalPrice().getCurrency()
            );
        }

        String newId = "pay-" + String.format("%04d", idCounter.getAndIncrement());
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment(
                newId,
                request.getBookingId(),
                new Amount(request.getAmount().getValue(), request.getAmount().getCurrency()),
                request.getMethod(),
                "PENDING",
                null,
                now,
                null
        );

        Payment savedPayment = paymentRepository.save(payment);

        System.out.println("[IPC SUCCESS] Payment " + newId + " created for booking " +
                request.getBookingId() + " (validated via Booking Service)");
        System.out.println("[ASYNC] Starting payment processing...");

        // Process payment asynchronously
        processPaymentAsync(savedPayment);

        return convertToDTO(savedPayment);
    }

    public RefundResponse refundPayment(String id, RefundRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new IllegalArgumentException(
                    "Can only refund completed payments. " +
                            "Current payment status: " + payment.getStatus() + ". " +
                            "Payment must be in 'COMPLETED' status to be refunded."
            );
        }

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new IllegalArgumentException(
                    "Payment " + id + " has already been refunded. " +
                            "Cannot refund the same payment twice."
            );
        }

        payment.setStatus("REFUNDED");
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        System.out.println("[REFUND] Payment " + id + " refunded successfully");

        RefundResponse response = new RefundResponse();
        response.setMessage("Payment refunded successfully");
        response.setPaymentId(payment.getId());
        response.setBookingId(payment.getBookingId());

        RefundResponse.AmountDTO amountDTO = new RefundResponse.AmountDTO();
        amountDTO.setValue(payment.getAmount().getValue());
        amountDTO.setCurrency(payment.getAmount().getCurrency());
        response.setAmount(amountDTO);

        response.setRefundedAt(payment.getProcessedAt());
        response.setReason(request != null && request.getReason() != null ?
                request.getReason() : "Not specified");

        return response;
    }

    public void deletePayment(String id) {
        if (paymentRepository.findById(id).isEmpty()) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found");
        }
        paymentRepository.deleteById(id);
        System.out.println("[DELETION] Payment " + id + " deleted from system");
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBookingId());

        PaymentDTO.AmountDTO amountDTO = new PaymentDTO.AmountDTO();
        amountDTO.setValue(payment.getAmount().getValue());
        amountDTO.setCurrency(payment.getAmount().getCurrency());
        dto.setAmount(amountDTO);

        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setProcessedAt(payment.getProcessedAt());

        return dto;
    }

    private void validatePaymentRequest(CreatePaymentRequest request) {
        if (request.getBookingId() == null || request.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required and cannot be empty");
        }

        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Payment amount is required");
        }

        if (request.getAmount().getValue() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive. Provided: " + request.getAmount().getValue()
            );
        }

        if (request.getAmount().getCurrency() == null || request.getAmount().getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (!request.getAmount().getCurrency().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter ISO code (e.g., EUR, USD). Provided: " +
                            request.getAmount().getCurrency()
            );
        }

        if (request.getMethod() == null || request.getMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required and cannot be empty");
        }

        if (!isValidPaymentMethod(request.getMethod())) {
            throw new IllegalArgumentException(
                    "Invalid payment method: " + request.getMethod() + ". " +
                            "Allowed values: CARD, PAYPAL, CASH"
            );
        }
    }

    private boolean isValidPaymentMethod(String method) {
        return List.of("CARD", "PAYPAL", "CASH").contains(method.toUpperCase());
    }

    private void processPaymentAsync(Payment payment) {
        new Thread(() -> {
            try {
                System.out.println("[ASYNC] Processing payment " + payment.getId() + "...");
                Thread.sleep(2000);

                // Simulate payment processing (95% success rate)
                boolean success = random.nextInt(100) < 95;

                if (success) {
                    payment.setStatus("COMPLETED");
                    payment.setTransactionId("txn-" + UUID.randomUUID().toString().substring(0, 8));
                    payment.setProcessedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    System.out.println("[ASYNC] Payment " + payment.getId() + " completed successfully");

                    // INTER-SERVICE CALL: Confirm booking after successful payment
                    System.out.println("[IPC] Confirming booking " + payment.getBookingId() + "...");
                    boolean confirmed = bookingServiceClient.confirmBooking(payment.getBookingId());

                    if (confirmed) {
                        System.out.println("[IPC SUCCESS] Booking " + payment.getBookingId() +
                                " confirmed in Booking Service after payment " + payment.getId());
                    } else {
                        System.err.println("[IPC WARNING] Payment succeeded but failed to confirm booking " +
                                payment.getBookingId() + " in Booking Service. Manual intervention may be required.");
                    }
                } else {
                    payment.setStatus("FAILED");
                    payment.setProcessedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    System.err.println("[ASYNC] Payment " + payment.getId() + " failed during processing");
                }

            } catch (InterruptedException e) {
                System.err.println("[ASYNC ERROR] Payment processing interrupted: " + e.getMessage());
                payment.setStatus("FAILED");
                payment.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[ASYNC ERROR] Unexpected error processing payment: " + e.getMessage());
                payment.setStatus("FAILED");
                payment.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                e.printStackTrace();
            }
        }).start();
    }
}