package com.nure.cinema.movie.repository;


import com.nure.cinema.movie.model.Movie;
import com.nure.cinema.movie.model.Session;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MovieRepository {

    private final List<Movie> movies = new ArrayList<>();
    private final List<Session> sessions = new ArrayList<>();

    public MovieRepository() {
        initializeData();
    }

    private void initializeData() {
        movies.add(new Movie(
                "mov-001",
                "Tini zabutykh predkiv",
                "Screen adaptation of Mykhailo Kotsiubynsky's novel",
                97,
                Arrays.asList("Drama", "History", "Romance"),
                8.2,
                "16+",
                "Dovzhenko Centre",
                "1965-09-04"
        ));

        movies.add(new Movie(
                "mov-002",
                "Dodomu",
                "Crimean Tatar Mustafa loses his eldest son in the war",
                96,
                Arrays.asList("Drama"),
                7.4,
                "16+",
                "Arthouse Traffic",
                "2019-11-07"
        ));

        movies.add(new Movie(
                "mov-003",
                "Moi dumky tykhi",
                "Freelance sound engineer Vadym receives an order",
                104,
                Arrays.asList("Comedy", "Drama"),
                7.8,
                "12+",
                "FILM.UA Distribution",
                "2020-01-16"
        ));

        sessions.add(new Session(
                "sess-1001",
                "mov-002",
                "hall-2",
                LocalDateTime.of(2025, 10, 20, 19, 0),
                LocalDateTime.of(2025, 10, 20, 20, 36),
                new Session.Price(8.0, "EUR"),
                160,
                "Scheduled"
        ));

        sessions.add(new Session(
                "sess-1002",
                "mov-003",
                "hall-1",
                LocalDateTime.of(2025, 10, 20, 21, 0),
                LocalDateTime.of(2025, 10, 20, 22, 44),
                new Session.Price(10.5, "EUR"),
                375,
                "Scheduled"
        ));
    }

    public List<Movie> findAll() {
        return new ArrayList<>(movies);
    }

    public Optional<Movie> findById(String id) {
        return movies.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    public List<Movie> searchByTitle(String searchText) {
        String searchLower = searchText.toLowerCase();
        return movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(searchLower) ||
                        m.getDescription().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }

    public Movie save(Movie movie) {
        movies.removeIf(m -> m.getId().equals(movie.getId()));
        movies.add(movie);
        return movie;
    }

    public void deleteById(String id) {
        movies.removeIf(m -> m.getId().equals(id));
    }

    public List<Session> findAllSessions() {
        return new ArrayList<>(sessions);
    }

    public Optional<Session> findSessionById(String id) {
        return sessions.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public List<Session> findSessionsByMovieId(String movieId) {
        return sessions.stream()
                .filter(s -> s.getMovieId().equals(movieId))
                .collect(Collectors.toList());
    }

    public Session saveSession(Session session) {
        sessions.removeIf(s -> s.getId().equals(session.getId()));
        sessions.add(session);
        return session;
    }

    public void deleteSessionById(String id) {
        sessions.removeIf(s -> s.getId().equals(id));
    }
}