package com.nure.cinema.booking.dto;

public class UpdateBookingRequest {
    private String status;
    private String notes;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}