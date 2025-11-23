package com.nure.PZ2.booking.model;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {
    private String id;
    private String sessionId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private List<Seat> seats;
    private Price totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private String notes;

    public Booking() {}

    public Booking(String id, String sessionId, String userId, String customerName,
                   String customerEmail, List<Seat> seats, Price totalPrice, String status,
                   LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime confirmedAt,
                   String notes) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.notes = notes;
    }

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

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    public Price getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Price totalPrice) { this.totalPrice = totalPrice; }

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

    // Inner classes
    public static class Seat {
        private int row;
        private int number;
        private String seatId;

        public Seat() {}

        public Seat(int row, int number, String seatId) {
            this.row = row;
            this.number = number;
            this.seatId = seatId;
        }

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }

        public String getSeatId() { return seatId; }
        public void setSeatId(String seatId) { this.seatId = seatId; }
    }

    public static class Price {
        private double value;
        private String currency;

        public Price() {}

        public Price(double value, String currency) {
            this.value = value;
            this.currency = currency;
        }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}