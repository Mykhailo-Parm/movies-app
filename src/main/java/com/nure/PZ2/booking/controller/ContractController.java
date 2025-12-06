package com.nure.PZ2.booking.controller;

import com.nure.PZ2.common.validation.SchemaValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for exposing inter-service contracts (JSON Schemas)
 * Useful for documentation and validation testing
 */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Service Contracts", description = "View inter-service communication contracts")
public class ContractController {

    private final SchemaValidator schemaValidator;

    public ContractController(SchemaValidator schemaValidator) {
        this.schemaValidator = schemaValidator;
    }

    @GetMapping
    @Operation(summary = "List all available contracts",
            description = "Returns list of all defined inter-service contracts")
    public ResponseEntity<Map<String, String>> getAllContracts() {
        Map<String, String> contracts = new HashMap<>();
        contracts.put("MovieSession", "Schema for Movie Service -> Booking Service communication");
        contracts.put("Booking", "Schema for Booking Service -> Payment Service communication");
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/{schemaName}")
    @Operation(summary = "Get specific contract schema",
            description = "Returns JSON Schema for a specific contract")
    public ResponseEntity<String> getContract(@PathVariable String schemaName) {
        String schema = schemaValidator.getSchema(schemaName);

        if (schema == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(schema);
    }

    @PostMapping("/{schemaName}/validate")
    @Operation(summary = "Validate JSON against contract",
            description = "Test if provided JSON matches the contract schema")
    public ResponseEntity<ValidationResult> validateJson(
            @PathVariable String schemaName,
            @RequestBody String json) {

        boolean isValid = schemaValidator.validate(schemaName, json);

        ValidationResult result = new ValidationResult();
        result.setSchemaName(schemaName);
        result.setValid(isValid);
        result.setMessage(isValid ? "JSON is valid" : "JSON does not match schema");

        return ResponseEntity.ok(result);
    }

    // DTO for validation result
    public static class ValidationResult {
        private String schemaName;
        private boolean valid;
        private String message;

        public String getSchemaName() { return schemaName; }
        public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}