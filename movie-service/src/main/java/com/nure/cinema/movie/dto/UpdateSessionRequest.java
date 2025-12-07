package com.nure.cinema.movie.dto;

import java.time.LocalDateTime;

public class UpdateSessionRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer availableSeats;
    private String status;

    // Getters and Setters
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}