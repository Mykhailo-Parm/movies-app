package com.nure.PZ2.booking.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.PZ2.booking.dto.MovieSessionDTO;
import com.nure.PZ2.common.validation.SchemaValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for inter-service communication with Movie Service
 * Includes JSON Schema validation for contract verification
 */
@Component
public class MovieServiceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SchemaValidator schemaValidator;
    private final String movieServiceUrl;

    public MovieServiceClient(
            @Value("${services.movie.url:http://localhost:8081/api}") String movieServiceUrl,
            ObjectMapper objectMapper,
            SchemaValidator schemaValidator) {
        this.movieServiceUrl = movieServiceUrl;
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get session details from Movie Service with contract validation
     * @param sessionId session identifier
     * @return session DTO or null if not found/error
     */
    public MovieSessionDTO getSession(String sessionId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(movieServiceUrl + "/movies/sessions/" + sessionId))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // VALIDATE: Check if response matches expected MovieSession schema
                boolean isValid = schemaValidator.validate("MovieSession", responseBody);
                if (!isValid) {
                    System.err.println("[CONTRACT VIOLATION] Movie Service returned invalid MovieSession structure");
                    System.err.println("Response: " + responseBody);
                    return null;
                }

                System.out.println("[CONTRACT OK] Movie Service response validated successfully");
                return objectMapper.readValue(responseBody, MovieSessionDTO.class);

            } else if (response.statusCode() == 404) {
                System.err.println("Session not found in Movie Service: " + sessionId);
                return null;
            } else {
                System.err.println("Movie Service returned status: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Failed to call Movie Service: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if session exists and is available
     * @param sessionId session identifier
     * @return true if session exists and has available seats
     */
    public boolean isSessionAvailable(String sessionId) {
        MovieSessionDTO session = getSession(sessionId);
        return session != null &&
                "Scheduled".equals(session.getStatus()) &&
                session.getAvailableSeats() > 0;
    }

    /**
     * Health check for Movie Service
     * @return true if Movie Service is reachable
     */
    public boolean isServiceHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(movieServiceUrl + "/movies"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}