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
                .orElseThrow(() -> new PaymentNotFoundException("Payment for booking " + bookingId + " not found"));
        return convertToDTO(payment);
    }

    public List<PaymentDTO> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO createPayment(CreatePaymentRequest request) {
        validatePaymentRequest(request);

        // INTER-SERVICE CALL: Validate booking exists in Booking Service
        BookingInfoDTO booking = bookingServiceClient.getBooking(request.getBookingId());
        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking " + request.getBookingId() + " not found in Booking Service"
            );
        }

        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalArgumentException(
                    "Booking " + request.getBookingId() + " is not in valid state for payment. Status: " + booking.getStatus()
            );
        }

        // Validate payment amount matches booking total
        if (Math.abs(request.getAmount().getValue() - booking.getTotalPrice().getValue()) > 0.01) {
            throw new IllegalArgumentException(
                    "Payment amount (" + request.getAmount().getValue() +
                            ") does not match booking total (" + booking.getTotalPrice().getValue() + ")"
            );
        }

        if (paymentRepository.existsCompletedPaymentForBooking(request.getBookingId())) {
            throw new PaymentAlreadyExistsException(
                    "Payment already completed for booking " + request.getBookingId()
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

        System.out.println("[IPC] Payment " + newId + " created for booking " +
                request.getBookingId() + " from Booking Service");

        processPaymentAsync(savedPayment);

        return convertToDTO(savedPayment);
    }

    public RefundResponse refundPayment(String id, RefundRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Can only refund completed payments");
        }

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Payment already refunded");
        }

        payment.setStatus("REFUNDED");
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        RefundResponse response = new RefundResponse();
        response.setMessage("Payment refunded successfully");
        response.setPaymentId(payment.getId());
        response.setBookingId(payment.getBookingId());

        RefundResponse.AmountDTO amountDTO = new RefundResponse.AmountDTO();
        amountDTO.setValue(payment.getAmount().getValue());
        amountDTO.setCurrency(payment.getAmount().getCurrency());
        response.setAmount(amountDTO);

        response.setRefundedAt(payment.getProcessedAt());
        response.setReason(request != null ? request.getReason() : "Not specified");

        return response;
    }

    public void deletePayment(String id) {
        if (!paymentRepository.findById(id).isPresent()) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found");
        }
        paymentRepository.deleteById(id);
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
        if (request.getBookingId() == null || request.getBookingId().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (request.getAmount() == null || request.getAmount().getValue() <= 0) {
            throw new IllegalArgumentException("Valid payment amount is required");
        }
        if (request.getMethod() == null || request.getMethod().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (!isValidPaymentMethod(request.getMethod())) {
            throw new IllegalArgumentException(
                    "Invalid payment method. Allowed: CARD, PAYPAL, CASH"
            );
        }
    }

    private boolean isValidPaymentMethod(String method) {
        return "CARD".equals(method) || "PAYPAL".equals(method) || "CASH".equals(method);
    }

    private void processPaymentAsync(Payment payment) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);

                boolean success = random.nextInt(100) < 95;

                if (success) {
                    payment.setStatus("COMPLETED");
                    payment.setTransactionId("txn-" + UUID.randomUUID().toString().substring(0, 8));

                    // INTER-SERVICE CALL: Confirm booking after successful payment
                    boolean confirmed = bookingServiceClient.confirmBooking(payment.getBookingId());
                    if (confirmed) {
                        System.out.println("[IPC] Booking " + payment.getBookingId() +
                                " confirmed in Booking Service after payment " + payment.getId());
                    } else {
                        System.err.println("[IPC] Failed to confirm booking " + payment.getBookingId() +
                                " in Booking Service");
                    }
                } else {
                    payment.setStatus("FAILED");
                }

                payment.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                System.out.println("Payment " + payment.getId() + " processed: " + payment.getStatus());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}