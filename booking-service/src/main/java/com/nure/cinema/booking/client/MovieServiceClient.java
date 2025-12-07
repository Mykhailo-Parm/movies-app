package com.nure.cinema.booking.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.cinema.booking.dto.MovieSessionDTO;
import com.nure.cinema.booking.validation.SchemaValidator;
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
 * Enhanced IPC Client з підтримкою Service Discovery та Client-Side Load Balancing
 *
 * Відмінності від попередньої версії:
 * - Використовує DiscoveryClient для пошуку інстансів Movie Service
 * - Реалізує client-side load balancing (round-robin)
 * - Автоматично fail-over на інший інстанс при збої
 */
@Component
public class MovieServiceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SchemaValidator schemaValidator;
    private final DiscoveryClient discoveryClient;

    public String getMovieServiceName() {
        return movieServiceName;
    }

    private final String movieServiceName;
    private final Random random = new Random();

    public MovieServiceClient(
            @Value("${services.movie.name:movie-service}") String movieServiceName,
            ObjectMapper objectMapper,
            SchemaValidator schemaValidator,
            DiscoveryClient discoveryClient) {
        this.movieServiceName = movieServiceName;
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
        this.discoveryClient = discoveryClient;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Отримати доступний інстанс Movie Service з балансуванням навантаження
     */
    public String getMovieServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(movieServiceName);

        if (instances == null || instances.isEmpty()) {
            System.err.println("[SERVICE DISCOVERY] No instances of " + movieServiceName + " available");
            return null;
        }

        // Client-side load balancing: випадковий вибір інстансу
        ServiceInstance instance = instances.get(random.nextInt(instances.size()));
        String url = instance.getUri().toString() + "/api";

        System.out.println("[LOAD BALANCING] Selected instance: " + instance.getInstanceId() +
                " at " + url + " (available: " + instances.size() + ")");

        return url;
    }

    /**
     * Get session з автоматичним fail-over
     */
    public MovieSessionDTO getSession(String sessionId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(movieServiceName);

        if (instances == null || instances.isEmpty()) {
            System.err.println("[IPC ERROR] Movie Service not available in Eureka");
            return null;
        }

        // Спробувати всі доступні інстанси
        for (ServiceInstance instance : instances) {
            try {
                String serviceUrl = instance.getUri().toString() + "/api";
                System.out.println("[IPC] Trying instance: " + instance.getInstanceId() + " at " + serviceUrl);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serviceUrl + "/movies/sessions/" + sessionId))
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
                    boolean isValid = schemaValidator.validate("MovieSession", responseBody);
                    if (!isValid) {
                        System.err.println("[CONTRACT VIOLATION] Invalid MovieSession from " +
                                instance.getInstanceId());
                        continue; // спробувати інший інстанс
                    }

                    System.out.println("[IPC SUCCESS] Response from " + instance.getInstanceId() +
                            " validated successfully");
                    return objectMapper.readValue(responseBody, MovieSessionDTO.class);

                } else if (response.statusCode() == 404) {
                    System.err.println("[IPC] Session not found (404) from " + instance.getInstanceId());
                    return null; // сесія не існує, не пробувати інші інстанси
                }

            } catch (Exception e) {
                System.err.println("[IPC FAIL] Instance " + instance.getInstanceId() +
                        " failed: " + e.getMessage());
                // продовжити спробу з наступним інстансом
            }
        }

        System.err.println("[IPC ERROR] All Movie Service instances failed");
        return null;
    }

    /**
     * Check session availability
     */
    public boolean isSessionAvailable(String sessionId) {
        MovieSessionDTO session = getSession(sessionId);
        return session != null &&
                "Scheduled".equals(session.getStatus()) &&
                session.getAvailableSeats() > 0;
    }

    /**
     * Health check
     */
    public boolean isServiceHealthy() {
        List<ServiceInstance> instances = discoveryClient.getInstances(movieServiceName);
        return instances != null && !instances.isEmpty();
    }

    /**
     * Get service info for debugging
     */
    public String getServiceInfo() {
        List<ServiceInstance> instances = discoveryClient.getInstances(movieServiceName);
        if (instances == null || instances.isEmpty()) {
            return "No instances available";
        }
        return instances.size() + " instance(s) available: " +
                instances.stream()
                        .map(ServiceInstance::getInstanceId)
                        .toList();
    }
}