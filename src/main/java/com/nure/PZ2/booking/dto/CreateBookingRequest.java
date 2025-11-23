package com.nure.PZ2.booking.dto;

import java.util.List;

public class CreateBookingRequest {
    private String sessionId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private List<SeatRequest> seats;
    private String notes;

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<SeatRequest> getSeats() { return seats; }
    public void setSeats(List<SeatRequest> seats) { this.seats = seats; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class SeatRequest {
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
}
