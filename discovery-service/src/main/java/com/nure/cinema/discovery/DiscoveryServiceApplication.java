package com.nure.cinema.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Service - Eureka Server
 *
 * Цей сервіс відповідає за:
 * - Реєстрацію всіх мікросервісів
 * - Надання інформації про доступні інстанси
 * - Health checking мікросервісів
 * - Динамічне виявлення сервісів
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("   EUREKA DISCOVERY SERVICE STARTED");
        System.out.println("=".repeat(70));
        System.out.println("   Port: 8761");
        System.out.println("   Dashboard: http://localhost:8761");
        System.out.println("   Status: http://localhost:8761/eureka/apps");
        System.out.println("=".repeat(70) + "\n");
        System.out.println("Чекаємо на реєстрацію мікросервісів...\n");
    }
}