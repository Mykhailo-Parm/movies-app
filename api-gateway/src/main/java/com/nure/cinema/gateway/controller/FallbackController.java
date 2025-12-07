package com.nure.cinema.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback контролер для обробки збоїв мікросервісів
 * Надає клієнтам зрозумілі повідомлення про помилки
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/movies")
    public ResponseEntity<Map<String, Object>> movieServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Movie Service Unavailable");
        response.put("message", "Movie Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        System.err.println("[FALLBACK] Movie Service unavailable at " + LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> bookingServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Booking Service Unavailable");
        response.put("message", "Booking Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        System.err.println("[FALLBACK] Booking Service unavailable at " + LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Payment Service Unavailable");
        response.put("message", "Payment Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

        System.err.println("[FALLBACK] Payment Service unavailable at " + LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}