package com.nure.cinema.payment.dto;

import java.time.LocalDateTime;

/**
 * DTO for inter-service communication
 * Represents Booking from Booking Service
 */
public class BookingInfoDTO {
    private String id;
    private String sessionId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private PriceDTO totalPrice;
    private String status;
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public PriceDTO getTotalPrice() { return totalPrice; }
    public void setTotalPrice(PriceDTO totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class PriceDTO {
        private double value;
        private String currency;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}