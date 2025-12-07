package com.nure.cinema.movie;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Movie Service - Управління фільмами та сеансами
 *
 * Функції:
 * - CRUD операції з фільмами
 * - CRUD операції з сеансами
 * - Пошук фільмів
 * - Надає дані для Booking Service через IPC
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MovieServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieServiceApplication.class, args);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("   MOVIE SERVICE STARTED");
        System.out.println("=".repeat(70));
        System.out.println("   Port: 8081");
        System.out.println("   Direct Access: http://localhost:8081/api/movies");
        System.out.println("   Via Gateway:   http://localhost:8080/api/movies");
        System.out.println("   Swagger UI:    http://localhost:8081/swagger-ui.html");
        System.out.println("   Eureka:        http://localhost:8761");
        System.out.println("=".repeat(70));
        System.out.println("   Status: Реєстрація в Eureka Discovery Service...");
        System.out.println("=".repeat(70) + "\n");
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }
}