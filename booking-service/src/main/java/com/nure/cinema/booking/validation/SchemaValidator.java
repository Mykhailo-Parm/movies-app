package com.nure.cinema.booking.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON Schema validator for inter-service contract validation
 * Defines expected structure of DTOs exchanged between microservices
 */
@Component
public class SchemaValidator {

    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemas;

    public SchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemas = new HashMap<>();
        initializeSchemas();
    }

    private void initializeSchemas() {
        // Schema for MovieSession DTO (Movie Service -> Booking Service)
        String sessionSchema = """
            {
              "type": "object",
              "required": ["id", "movieId", "hallId", "startTime", "endTime", "price", "availableSeats", "status"],
              "properties": {
                "id": {"type": "string", "pattern": "^sess-[0-9]+$"},
                "movieId": {"type": "string", "pattern": "^mov-[0-9]+$"},
                "hallId": {"type": "string"},
                "startTime": {"type": "string", "format": "date-time"},
                "endTime": {"type": "string", "format": "date-time"},
                "price": {
                  "type": "object",
                  "required": ["value", "currency"],
                  "properties": {
                    "value": {"type": "number", "minimum": 0},
                    "currency": {"type": "string", "pattern": "^[A-Z]{3}$"}
                  }
                },
                "availableSeats": {"type": "integer", "minimum": 0},
                "status": {"type": "string", "enum": ["Scheduled", "Cancelled", "Completed"]}
              }
            }
            """;

        // Schema for Booking DTO (Booking Service -> Payment Service)
        String bookingSchema = """
            {
              "type": "object",
              "required": ["id", "sessionId", "userId", "customerName", "customerEmail", "totalPrice", "status"],
              "properties": {
                "id": {"type": "string", "pattern": "^bk-[0-9]+$"},
                "sessionId": {"type": "string", "pattern": "^sess-[0-9]+$"},
                "userId": {"type": "string"},
                "customerName": {"type": "string", "minLength": 1},
                "customerEmail": {"type": "string", "format": "email"},
                "totalPrice": {
                  "type": "object",
                  "required": ["value", "currency"],
                  "properties": {
                    "value": {"type": "number", "minimum": 0},
                    "currency": {"type": "string", "pattern": "^[A-Z]{3}$"}
                  }
                },
                "status": {"type": "string", "enum": ["PENDING", "CONFIRMED", "CANCELLED"]}
              }
            }
            """;

        try {
            schemas.put("MovieSession", objectMapper.readTree(sessionSchema));
            schemas.put("Booking", objectMapper.readTree(bookingSchema));
        } catch (Exception e) {
            System.err.println("Failed to initialize JSON schemas: " + e.getMessage());
        }
    }

    /**
     * Validate JSON data against schema
     * @param schemaName name of schema to validate against
     * @param json JSON string to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(String schemaName, String json) {
        try {
            JsonNode schema = schemas.get(schemaName);
            if (schema == null) {
                System.err.println("Schema not found: " + schemaName);
                return false;
            }

            JsonNode data = objectMapper.readTree(json);

            // Basic validation: check required fields exist
            JsonNode required = schema.get("required");
            if (required != null && required.isArray()) {
                for (JsonNode field : required) {
                    String fieldName = field.asText();
                    if (!data.has(fieldName)) {
                        System.err.println("Missing required field: " + fieldName);
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get schema as string for documentation
     * @param schemaName name of schema
     * @return schema JSON string
     */
    public String getSchema(String schemaName) {
        try {
            JsonNode schema = schemas.get(schemaName);
            return schema != null ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema) : null;
        } catch (Exception e) {
            return null;
        }
    }
}