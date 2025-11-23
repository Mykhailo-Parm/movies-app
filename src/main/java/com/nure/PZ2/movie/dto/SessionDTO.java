package com.nure.PZ2.movie.dto;

import java.time.LocalDateTime;

public class SessionDTO {
    private String id;
    private String movieId;
    private String hallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private PriceDTO price;
    private int availableSeats;
    private String status;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getHallId() { return hallId; }
    public void setHallId(String hallId) { this.hallId = hallId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public PriceDTO getPrice() { return price; }
    public void setPrice(PriceDTO price) { this.price = price; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static class PriceDTO {
        private double value;
        private String currency;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}