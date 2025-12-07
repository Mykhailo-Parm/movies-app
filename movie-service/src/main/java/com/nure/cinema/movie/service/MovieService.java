package com.nure.cinema.movie.service;


import com.nure.cinema.movie.dto.*;
import com.nure.cinema.movie.exception.MovieNotFoundException;
import com.nure.cinema.movie.exception.SessionNotFoundException;
import com.nure.cinema.movie.model.Movie;
import com.nure.cinema.movie.model.Session;
import com.nure.cinema.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with ID " + id + " not found"));
        return convertToDTO(movie);
    }

    public List<MovieDTO> searchMovies(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return getAllMovies();
        }
        return movieRepository.searchByTitle(searchText).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO createMovie(CreateMovieRequest request) {
        validateMovieRequest(request);

        Movie movie = new Movie(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getDurationMinutes(),
                request.getGenres(),
                request.getRating(),
                request.getAgeRestriction(),
                request.getDistributor(),
                request.getReleaseDate()
        );

        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    public MovieDTO updateMovie(String id, UpdateMovieRequest request) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with ID " + id + " not found"));

        if (request.getTitle() != null) {
            existingMovie.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingMovie.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            existingMovie.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getGenres() != null) {
            existingMovie.setGenres(request.getGenres());
        }
        if (request.getRating() != null) {
            existingMovie.setRating(request.getRating());
        }

        Movie updatedMovie = movieRepository.save(existingMovie);
        return convertToDTO(updatedMovie);
    }

    public void deleteMovie(String id) {
        if (!movieRepository.findById(id).isPresent()) {
            throw new MovieNotFoundException("Movie with ID " + id + " not found");
        }
        movieRepository.deleteById(id);
    }

    public List<SessionDTO> getAllSessions() {
        return movieRepository.findAllSessions().stream()
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());
    }

    public List<SessionDTO> getSessionsByMovieId(String movieId) {
        if (!movieRepository.findById(movieId).isPresent()) {
            throw new MovieNotFoundException("Movie with ID " + movieId + " not found");
        }

        return movieRepository.findSessionsByMovieId(movieId).stream()
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());
    }

    public SessionDTO getSessionById(String sessionId) {
        Session session = movieRepository.findSessionById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session with ID " + sessionId + " not found"));
        return convertSessionToDTO(session);
    }

    public SessionDTO createSession(CreateSessionRequest request) {
        validateSessionRequest(request);

        if (!movieRepository.findById(request.getMovieId()).isPresent()) {
            throw new MovieNotFoundException("Movie with ID " + request.getMovieId() + " not found");
        }

        Session session = new Session(
                request.getId(),
                request.getMovieId(),
                request.getHallId(),
                request.getStartTime(),
                request.getEndTime(),
                new Session.Price(request.getPrice().getValue(), request.getPrice().getCurrency()),
                request.getAvailableSeats(),
                request.getStatus()
        );

        Session savedSession = movieRepository.saveSession(session);
        return convertSessionToDTO(savedSession);
    }

    public SessionDTO updateSession(String id, UpdateSessionRequest request) {
        Session existingSession = movieRepository.findSessionById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session with ID " + id + " not found"));

        if (request.getStartTime() != null) {
            existingSession.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            existingSession.setEndTime(request.getEndTime());
        }
        if (request.getAvailableSeats() != null) {
            existingSession.setAvailableSeats(request.getAvailableSeats());
        }
        if (request.getStatus() != null) {
            existingSession.setStatus(request.getStatus());
        }

        Session updatedSession = movieRepository.saveSession(existingSession);
        return convertSessionToDTO(updatedSession);
    }

    public void deleteSession(String id) {
        if (!movieRepository.findSessionById(id).isPresent()) {
            throw new SessionNotFoundException("Session with ID " + id + " not found");
        }
        movieRepository.deleteSessionById(id);
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setGenres(movie.getGenres());
        dto.setRating(movie.getRating());
        dto.setAgeRestriction(movie.getAgeRestriction());
        dto.setDistributor(movie.getDistributor());
        dto.setReleaseDate(movie.getReleaseDate());
        return dto;
    }

    private SessionDTO convertSessionToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setMovieId(session.getMovieId());
        dto.setHallId(session.getHallId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());

        SessionDTO.PriceDTO priceDTO = new SessionDTO.PriceDTO();
        priceDTO.setValue(session.getPrice().getValue());
        priceDTO.setCurrency(session.getPrice().getCurrency());
        dto.setPrice(priceDTO);

        dto.setAvailableSeats(session.getAvailableSeats());
        dto.setStatus(session.getStatus());
        return dto;
    }

    private void validateMovieRequest(CreateMovieRequest request) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Movie title is required");
        }
        if (request.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (request.getRating() < 0 || request.getRating() > 10) {
            throw new IllegalArgumentException("Rating must be between 0 and 10");
        }
    }

    private void validateSessionRequest(CreateSessionRequest request) {
        if (request.getMovieId() == null || request.getMovieId().isEmpty()) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}