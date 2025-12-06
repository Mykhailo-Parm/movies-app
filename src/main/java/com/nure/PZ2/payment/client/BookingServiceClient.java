package com.nure.PZ2.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.PZ2.payment.dto.BookingInfoDTO;
import com.nure.PZ2.common.validation.SchemaValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Enhanced client for inter-service communication with Booking Service
 * Includes comprehensive error handling and JSON Schema validation
 */
@Component
public class BookingServiceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SchemaValidator schemaValidator;
    private final String bookingServiceUrl;

    public BookingServiceClient(
            @Value("${services.booking.url:http://localhost:8082/api}") String bookingServiceUrl,
            ObjectMapper objectMapper,
            SchemaValidator schemaValidator) {
        this.bookingServiceUrl = bookingServiceUrl;
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get booking details from Booking Service with contract validation
     * @param bookingId booking identifier
     * @return booking DTO or null if not found/error
     */
    public BookingInfoDTO getBooking(String bookingId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookingServiceUrl + "/bookings/" + bookingId))
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

                // VALIDATE: Check if response matches expected Booking schema
                boolean isValid = schemaValidator.validate("Booking", responseBody);
                if (!isValid) {
                    System.err.println("[CONTRACT VIOLATION] Booking Service returned invalid Booking structure");
                    System.err.println("Response: " + responseBody);
                    return null;
                }

                System.out.println("[CONTRACT OK] Booking Service response validated successfully");
                return objectMapper.readValue(responseBody, BookingInfoDTO.class);

            } else if (response.statusCode() == 404) {
                System.err.println("[IPC ERROR] Booking not found in Booking Service: " + bookingId);
                return null;
            } else {
                System.err.println("[IPC ERROR] Booking Service returned unexpected status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return null;
            }

        } catch (java.net.ConnectException e) {
            System.err.println("[IPC ERROR] Cannot connect to Booking Service at " + bookingServiceUrl);
            System.err.println("Is Booking Service running?");
            return null;
        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("[IPC ERROR] Booking Service request timed out after 5 seconds");
            return null;
        } catch (java.io.IOException e) {
            System.err.println("[IPC ERROR] Network error calling Booking Service: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            System.err.println("[IPC ERROR] Request to Booking Service was interrupted");
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            System.err.println("[IPC ERROR] Unexpected error calling Booking Service: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update booking status to CONFIRMED after successful payment
     * @param bookingId booking identifier
     * @return true if update successful
     */
    public boolean confirmBooking(String bookingId) {
        try {
            String jsonBody = "{\"status\": \"CONFIRMED\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookingServiceUrl + "/bookings/" + bookingId))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                System.out.println("[IPC SUCCESS] Successfully confirmed booking " + bookingId);
                return true;
            } else if (response.statusCode() == 404) {
                System.err.println("[IPC ERROR] Booking " + bookingId + " not found for confirmation");
                return false;
            } else if (response.statusCode() == 400) {
                System.err.println("[IPC ERROR] Cannot confirm booking " + bookingId + ": " + response.body());
                return false;
            } else {
                System.err.println("[IPC ERROR] Failed to confirm booking. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }

        } catch (java.net.ConnectException e) {
            System.err.println("[IPC ERROR] Cannot connect to Booking Service for confirmation");
            return false;
        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("[IPC ERROR] Booking confirmation request timed out");
            return false;
        } catch (Exception e) {
            System.err.println("[IPC ERROR] Failed to confirm booking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if booking exists and is valid for payment
     * @param bookingId booking identifier
     * @return true if booking can be paid
     */
    public boolean isBookingValidForPayment(String bookingId) {
        BookingInfoDTO booking = getBooking(bookingId);
        return booking != null &&
                ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()));
    }

    /**
     * Health check for Booking Service
     * @return true if Booking Service is reachable
     */
    public boolean isServiceHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookingServiceUrl + "/bookings"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[HEALTH CHECK] Booking Service is not healthy: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get service URL for error messages
     */
    public String getServiceUrl() {
        return bookingServiceUrl;
    }
}