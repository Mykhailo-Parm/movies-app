package com.nure.PZ2.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "payment");
        SpringApplication app = new SpringApplication(PaymentServiceApplication.class);
        app.run(args);

        System.out.println("\n============================================================");
        System.out.println("   PAYMENT SERVICE STARTED");
        System.out.println("   Port: 8083");
        System.out.println("   Swagger UI: http://localhost:8083/swagger-ui.html");
        System.out.println("   API Docs: http://localhost:8083/api-docs");
        System.out.println("   Supports: JSON & XML (Content Negotiation)");
        System.out.println("============================================================\n");
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }
}