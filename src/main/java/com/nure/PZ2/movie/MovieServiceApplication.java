package com.nure.PZ2.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.nure.PZ2.movie",
        "com.nure.PZ2.common"
})
public class MovieServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "movie");
        SpringApplication app = new SpringApplication(MovieServiceApplication.class);
        app.run(args);

        System.out.println("\n============================================================");
        System.out.println("   MOVIE SERVICE STARTED");
        System.out.println("   Port: 8081");
        System.out.println("   Swagger UI: http://localhost:8081/swagger-ui.html");
        System.out.println("   API Docs: http://localhost:8081/api-docs");
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