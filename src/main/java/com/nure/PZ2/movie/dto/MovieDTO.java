package com.nure.PZ2.movie.dto;

import java.time.LocalDateTime;
import java.util.List;

// ============ MovieDTO ============
public class MovieDTO {
    private String id;
    private String title;
    private String description;
    private int durationMinutes;
    private List<String> genres;
    private double rating;
    private String ageRestriction;
    private String distributor;
    private String releaseDate;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(String ageRestriction) { this.ageRestriction = ageRestriction; }

    public String getDistributor() { return distributor; }
    public void setDistributor(String distributor) { this.distributor = distributor; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
}
