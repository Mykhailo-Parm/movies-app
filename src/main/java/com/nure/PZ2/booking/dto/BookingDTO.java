package com.nure.PZ2.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

// ============ BookingDTO ============
public class BookingDTO {
    private String id;
    private String sessionId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private List<SeatDTO> seats;
    private PriceDTO totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private String notes;

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

    public List<SeatDTO> getSeats() { return seats; }
    public void setSeats(List<SeatDTO> seats) { this.seats = seats; }

    public PriceDTO getTotalPrice() { return totalPrice; }
    public void setTotalPrice(PriceDTO totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class SeatDTO {
        private int row;
        private int number;
        private String seatId;

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }

        public String getSeatId() { return seatId; }
        public void setSeatId(String seatId) { this.seatId = seatId; }
    }

    public static class PriceDTO {
        private double value;
        private String currency;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
