package com.nure.cinema.payment;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Payment Service - Обробка платежів
 *
 * Функції:
 * - CRUD операції з платежами
 * - Валідація бронювань через Booking Service (IPC)
 * - Асинхронна обробка платежів
 * - Підтвердження бронювань після успішної оплати
 * - Обробка повернень (refunds)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("   PAYMENT SERVICE STARTED");
        System.out.println("=".repeat(70));
        System.out.println("   Port: 8083");
        System.out.println("   Direct Access: http://localhost:8083/api/payments");
        System.out.println("   Via Gateway:   http://localhost:8080/api/payments");
        System.out.println("   Swagger UI:    http://localhost:8083/swagger-ui.html");
        System.out.println("   Eureka:        http://localhost:8761");
        System.out.println("=".repeat(70));
        System.out.println("   Status: Реєстрація в Eureka Discovery Service...");
        System.out.println("   IPC: Використовує Booking Service для валідації");
        System.out.println("=".repeat(70) + "\n");
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }
}