package com.nure.cinema.booking;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Booking Service - Управління бронюваннями квитків
 *
 * Функції:
 * - CRUD операції з бронюваннями
 * - Валідація сеансів через Movie Service (IPC)
 * - Перевірка доступності місць
 * - Підтвердження бронювань після оплати
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("   BOOKING SERVICE STARTED");
        System.out.println("=".repeat(70));
        System.out.println("   Port: 8082");
        System.out.println("   Direct Access: http://localhost:8082/api/bookings");
        System.out.println("   Via Gateway:   http://localhost:8080/api/bookings");
        System.out.println("   Swagger UI:    http://localhost:8082/swagger-ui.html");
        System.out.println("   Eureka:        http://localhost:8761");
        System.out.println("=".repeat(70));
        System.out.println("   Status: Реєстрація в Eureka Discovery Service...");
        System.out.println("   IPC: Використовує Movie Service для валідації сеансів");
        System.out.println("=".repeat(70) + "\n");
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }
}