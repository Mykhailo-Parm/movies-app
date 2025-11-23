package com.nure.PZ2.movie.model;

import java.time.LocalDateTime;

public class Session {
    private String id;
    private String movieId;
    private String hallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Price price;
    private int availableSeats;
    private String status;

    public Session() {}

    public Session(String id, String movieId, String hallId, LocalDateTime startTime,
                   LocalDateTime endTime, Price price, int availableSeats, String status) {
        this.id = id;
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.status = status;
    }

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

    public Price getPrice() { return price; }
    public void setPrice(Price price) { this.price = price; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Inner class for Price
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