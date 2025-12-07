package com.nure.cinema.payment.dto;

public class CreatePaymentRequest {
    private String bookingId;
    private AmountRequest amount;
    private String method;

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public AmountRequest getAmount() { return amount; }
    public void setAmount(AmountRequest amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public static class AmountRequest {
        private double value;
        private String currency;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}