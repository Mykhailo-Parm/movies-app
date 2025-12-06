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
 * Client for inter-service communication with Booking Service
 * Includes JSON Schema validation for contract verification
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
                System.err.println("Booking not found in Booking Service: " + bookingId);
                return null;
            } else {
                System.err.println("Booking Service returned status: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Failed to call Booking Service: " + e.getMessage());
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
                System.out.println("[IPC] Successfully confirmed booking " + bookingId);
                return true;
            } else {
                System.err.println("[IPC] Failed to confirm booking. Status: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Failed to confirm booking: " + e.getMessage());
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
}