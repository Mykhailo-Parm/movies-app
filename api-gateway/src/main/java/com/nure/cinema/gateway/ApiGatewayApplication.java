package com.nure.cinema.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - Єдина точка входу для всіх клієнтів
 *
 * Функції:
 * - Маршрутизація запитів до мікросервісів
 * - Балансування навантаження
 * - Ізоляція внутрішніх сервісів
 * - Централізоване логування
 * - Rate limiting (опціонально)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("   API GATEWAY STARTED");
        System.out.println("=".repeat(70));
        System.out.println("   Port: 8080");
        System.out.println("   Single Entry Point: http://localhost:8080");
        System.out.println("=".repeat(70));
        System.out.println("\n   📍 Маршрути:");
        System.out.println("   • Movies:   http://localhost:8080/api/movies/**");
        System.out.println("   • Bookings: http://localhost:8080/api/bookings/**");
        System.out.println("   • Payments: http://localhost:8080/api/payments/**");
        System.out.println("\n   🔒 Приховані сервіси (недоступні ззовні):");
        System.out.println("   • Internal services не мають прямих маршрутів");
        System.out.println("=".repeat(70) + "\n");
    }
}