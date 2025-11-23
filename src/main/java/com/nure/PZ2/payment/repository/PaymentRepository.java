package com.nure.PZ2.payment.repository;

import com.nure.PZ2.payment.model.Payment;
import com.nure.PZ2.payment.model.Payment.Amount;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PaymentRepository {

    private final List<Payment> payments = new ArrayList<>();

    public PaymentRepository() {
        initializeData();
    }

    private void initializeData() {
        payments.add(new Payment(
                "pay-5001",
                "bk-1001",
                new Amount(21.0, "EUR"),
                "CARD",
                "COMPLETED",
                "txn-a1b2c3d4",
                LocalDateTime.of(2025, 10, 10, 14, 31),
                LocalDateTime.of(2025, 10, 10, 14, 31, 15)
        ));
    }

    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    public Optional<Payment> findById(String id) {
        return payments.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Payment> findByBookingId(String bookingId) {
        return payments.stream()
                .filter(p -> p.getBookingId().equals(bookingId))
                .findFirst();
    }

    public List<Payment> findByStatus(String status) {
        return payments.stream()
                .filter(p -> p.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public List<Payment> findByMethod(String method) {
        return payments.stream()
                .filter(p -> p.getMethod().equals(method))
                .collect(Collectors.toList());
    }

    public Payment save(Payment payment) {
        payments.removeIf(p -> p.getId().equals(payment.getId()));
        payments.add(payment);
        return payment;
    }

    public void deleteById(String id) {
        payments.removeIf(p -> p.getId().equals(id));
    }

    public boolean existsByBookingId(String bookingId) {
        return payments.stream()
                .anyMatch(p -> p.getBookingId().equals(bookingId));
    }

    public boolean existsCompletedPaymentForBooking(String bookingId) {
        return payments.stream()
                .anyMatch(p -> p.getBookingId().equals(bookingId) &&
                        "COMPLETED".equals(p.getStatus()));
    }
}