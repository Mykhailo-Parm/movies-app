package com.nure.cinema.booking.controller;

import com.nure.cinema.booking.client.MovieServiceClient;
import com.nure.cinema.booking.dto.*;
import com.nure.cinema.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Service", description = "API for managing ticket bookings")
public class BookingController {

    private final BookingService bookingService;
    private final MovieServiceClient movieServiceClient;

    public BookingController(BookingService bookingService, MovieServiceClient movieServiceClient) {
        this.bookingService = bookingService;
        this.movieServiceClient = movieServiceClient;
    }

    @GetMapping
    @Operation(summary = "Get all bookings or filter by status/user",
            description = "Returns list of all bookings with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bookings")
    })
    public ResponseEntity<List<BookingDTO>> getBookings(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {

        List<BookingDTO> bookings;

        if (userId != null) {
            bookings = bookingService.getBookingsByUserId(userId);
        } else if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else {
            bookings = bookingService.getAllBookings();
        }

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID",
            description = "Returns details of a specific booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @PostMapping
    @Operation(summary = "Create new booking",
            description = "Creates a new ticket booking")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Seat already booked")
    })
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingRequest request) {
        BookingDTO createdBooking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking",
            description = "Updates an existing booking (status, notes)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable String id,
            @RequestBody UpdateBookingRequest request) {
        BookingDTO updatedBooking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking",
            description = "Cancels an existing booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Booking already cancelled")
    })
    public ResponseEntity<String> cancelBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking " + id + " cancelled successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking",
            description = "Permanently deletes a booking from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ============ NEW: Service Dependencies Health Check ============

    @GetMapping("/health/dependencies")
    @Operation(summary = "Check dependencies health",
            description = "Returns health status of dependent services (Movie Service)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dependencies checked successfully")
    })
    public ResponseEntity<Map<String, Object>> checkDependenciesHealth() {
        Map<String, Object> health = new HashMap<>();

        // Check Movie Service health
        boolean movieServiceHealthy = movieServiceClient.isServiceHealthy();
        String movieServiceInfo = movieServiceClient.getServiceInfo();
        String movieServiceUrl = movieServiceClient.getMovieServiceUrl();

        Map<String, Object> movieServiceStatus = new HashMap<>();
        movieServiceStatus.put("healthy", movieServiceHealthy);
        movieServiceStatus.put("info", movieServiceInfo);
        movieServiceStatus.put("selectedInstance", movieServiceUrl != null ? movieServiceUrl : "No instances available");

        health.put("movieService", movieServiceStatus);
        health.put("overallStatus", movieServiceHealthy ? "UP" : "DOWN");
        health.put("timestamp", java.time.LocalDateTime.now());

        HttpStatus status = movieServiceHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    @GetMapping("/debug/movie-service")
    @Operation(summary = "Get Movie Service connection info",
            description = "Returns detailed information about Movie Service connection for debugging")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connection info retrieved")
    })
    public ResponseEntity<Map<String, Object>> getMovieServiceDebugInfo() {
        Map<String, Object> debugInfo = new HashMap<>();

        debugInfo.put("serviceName", movieServiceClient.getMovieServiceName());
        debugInfo.put("serviceUrl", movieServiceClient.getMovieServiceUrl());
        debugInfo.put("serviceInfo", movieServiceClient.getServiceInfo());
        debugInfo.put("isHealthy", movieServiceClient.isServiceHealthy());
        debugInfo.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(debugInfo);
    }
}