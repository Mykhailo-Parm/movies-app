package com.nure.cinema.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.cinema.payment.dto.BookingInfoDTO;
import com.nure.cinema.payment.validation.SchemaValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * IPC Client з підтримкою Service Discovery та Client-Side Load Balancing
 */
@Component
public class BookingServiceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SchemaValidator schemaValidator;
    private final DiscoveryClient discoveryClient;
    private final String bookingServiceName;
    private final Random random = new Random();

    public BookingServiceClient(
            @Value("${services.booking.name:booking-service}") String bookingServiceName,
            ObjectMapper objectMapper,
            SchemaValidator schemaValidator,
            DiscoveryClient discoveryClient) {
        this.bookingServiceName = bookingServiceName;
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
        this.discoveryClient = discoveryClient;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get booking з автоматичним fail-over
     */
    public BookingInfoDTO getBooking(String bookingId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(bookingServiceName);

        if (instances == null || instances.isEmpty()) {
            System.err.println("[IPC ERROR] Booking Service not available in Eureka");
            return null;
        }

        for (ServiceInstance instance : instances) {
            try {
                String serviceUrl = instance.getUri().toString() + "/api";
                System.out.println("[IPC] Trying instance: " + instance.getInstanceId() + " at " + serviceUrl);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serviceUrl + "/bookings/" + bookingId))
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

                    // JSON Schema validation
                    boolean isValid = schemaValidator.validate("Booking", responseBody);
                    if (!isValid) {
                        System.err.println("[CONTRACT VIOLATION] Invalid Booking from " +
                                instance.getInstanceId());
                        continue;
                    }

                    System.out.println("[IPC SUCCESS] Response from " + instance.getInstanceId() +
                            " validated successfully");
                    return objectMapper.readValue(responseBody, BookingInfoDTO.class);

                } else if (response.statusCode() == 404) {
                    System.err.println("[IPC] Booking not found (404) from " + instance.getInstanceId());
                    return null;
                }

            } catch (Exception e) {
                System.err.println("[IPC FAIL] Instance " + instance.getInstanceId() +
                        " failed: " + e.getMessage());
            }
        }

        System.err.println("[IPC ERROR] All Booking Service instances failed");
        return null;
    }

    /**
     * Confirm booking після успішної оплати
     */
    public boolean confirmBooking(String bookingId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(bookingServiceName);

        if (instances == null || instances.isEmpty()) {
            System.err.println("[IPC ERROR] Booking Service not available for confirmation");
            return false;
        }

        for (ServiceInstance instance : instances) {
            try {
                String serviceUrl = instance.getUri().toString() + "/api";
                String jsonBody = "{\"status\": \"CONFIRMED\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serviceUrl + "/bookings/" + bookingId))
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
                    System.out.println("[IPC SUCCESS] Booking " + bookingId +
                            " confirmed via " + instance.getInstanceId());
                    return true;
                }

            } catch (Exception e) {
                System.err.println("[IPC FAIL] Confirmation failed on " +
                        instance.getInstanceId() + ": " + e.getMessage());
            }
        }

        return false;
    }

    public boolean isBookingValidForPayment(String bookingId) {
        BookingInfoDTO booking = getBooking(bookingId);
        return booking != null &&
                ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()));
    }

    public boolean isServiceHealthy() {
        List<ServiceInstance> instances = discoveryClient.getInstances(bookingServiceName);
        return instances != null && !instances.isEmpty();
    }

    public String getServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(bookingServiceName);
        if (instances == null || instances.isEmpty()) {
            return "Service not available in discovery";
        }
        return instances.size() + " instance(s) available";
    }
}