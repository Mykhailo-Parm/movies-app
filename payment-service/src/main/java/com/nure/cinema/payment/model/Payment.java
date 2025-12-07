package com.nure.cinema.payment.model;

import java.time.LocalDateTime;

public class Payment {
    private String id;
    private String bookingId;
    private Amount amount;
    private String method;
    private String status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public Payment() {}

    public Payment(String id, String bookingId, Amount amount, String method,
                   String status, String transactionId, LocalDateTime createdAt,
                   LocalDateTime processedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Amount getAmount() { return amount; }
    public void setAmount(Amount amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public static class Amount {
        private double value;
        private String currency;

        public Amount() {}

        public Amount(double value, String currency) {
            this.value = value;
            this.currency = currency;
        }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}