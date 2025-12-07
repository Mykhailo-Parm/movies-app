package com.nure.cinema.movie.dto;

import java.util.List;

public class UpdateMovieRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private List<String> genres;
    private Double rating;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
