package com.nure.cinema.movie.controller;


import com.nure.cinema.movie.dto.*;
import com.nure.cinema.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movie Service", description = "API for managing movies and sessions")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ============ MOVIE ENDPOINTS ============

    @GetMapping
    @Operation(summary = "Get all movies or search by title",
            description = "Returns list of all movies or filtered by search text")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies")
    })
    public ResponseEntity<List<MovieDTO>> getMovies(
            @RequestParam(required = false) String search) {
        List<MovieDTO> movies = search != null
                ? movieService.searchMovies(search)
                : movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID",
            description = "Returns details of a specific movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable String id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @PostMapping
    @Operation(summary = "Create new movie",
            description = "Creates a new movie in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movie created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<MovieDTO> createMovie(@RequestBody CreateMovieRequest request) {
        MovieDTO createdMovie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update movie",
            description = "Updates an existing movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<MovieDTO> updateMovie(
            @PathVariable String id,
            @RequestBody UpdateMovieRequest request) {
        MovieDTO updatedMovie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete movie",
            description = "Deletes a movie from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    // ============ SESSION ENDPOINTS (SUB-RESOURCES) ============

    @GetMapping("/sessions")
    @Operation(summary = "Get all sessions",
            description = "Returns list of all movie sessions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sessions")
    })
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        List<SessionDTO> sessions = movieService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Get sessions by movie ID",
            description = "Returns all sessions for a specific movie (Sub-resource)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sessions"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<List<SessionDTO>> getSessionsByMovieId(@PathVariable String id) {
        List<SessionDTO> sessions = movieService.getSessionsByMovieId(id);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session by ID",
            description = "Returns details of a specific session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable String sessionId) {
        SessionDTO session = movieService.getSessionById(sessionId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create new session",
            description = "Creates a new movie session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<SessionDTO> createSession(@RequestBody CreateSessionRequest request) {
        SessionDTO createdSession = movieService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @PutMapping("/sessions/{id}")
    @Operation(summary = "Update session",
            description = "Updates an existing session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session updated successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<SessionDTO> updateSession(
            @PathVariable String id,
            @RequestBody UpdateSessionRequest request) {
        SessionDTO updatedSession = movieService.updateSession(id, request);
        return ResponseEntity.ok(updatedSession);
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Delete session",
            description = "Deletes a session from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        movieService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}