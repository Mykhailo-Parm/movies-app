package com.nure.cinema.payment.dto;

import java.time.LocalDateTime;

public class RefundResponse {
    private String message;
    private String paymentId;
    private String bookingId;
    private AmountDTO amount;
    private LocalDateTime refundedAt;
    private String reason;

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public AmountDTO getAmount() { return amount; }
    public void setAmount(AmountDTO amount) { this.amount = amount; }

    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static class AmountDTO {
        private double value;
        private String currency;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}