package com.nure.cinema.payment.controller;

import com.nure.cinema.payment.client.BookingServiceClient;
import com.nure.cinema.payment.dto.*;
import com.nure.cinema.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Service", description = "API for managing payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingServiceClient bookingServiceClient;

    public PaymentController(PaymentService paymentService, BookingServiceClient bookingServiceClient) {
        this.paymentService = paymentService;
        this.bookingServiceClient = bookingServiceClient;
    }

    @GetMapping
    @Operation(summary = "Get all payments or filter by status",
            description = "Returns list of all payments with optional status filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
    })
    public ResponseEntity<List<PaymentDTO>> getPayments(
            @RequestParam(required = false) String bookingId,
            @RequestParam(required = false) String status) {

        if (bookingId != null) {
            PaymentDTO payment = paymentService.getPaymentByBookingId(bookingId);
            return ResponseEntity.ok(List.of(payment));
        }

        List<PaymentDTO> payments = status != null
                ? paymentService.getPaymentsByStatus(status)
                : paymentService.getAllPayments();

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID",
            description = "Returns details of a specific payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable String id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    @Operation(summary = "Create new payment",
            description = "Initiates a new payment for a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Payment already exists for this booking")
    })
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody CreatePaymentRequest request) {
        PaymentDTO createdPayment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment",
            description = "Initiates a refund for a completed payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Payment cannot be refunded")
    })
    public ResponseEntity<RefundResponse> refundPayment(
            @PathVariable String id,
            @RequestBody(required = false) RefundRequest request) {
        RefundResponse response = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment",
            description = "Permanently deletes a payment from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Payment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // ============ NEW: Service Dependencies Health Check ============

    @GetMapping("/health/dependencies")
    @Operation(summary = "Check dependencies health",
            description = "Returns health status of dependent services (Booking Service)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dependencies checked successfully")
    })
    public ResponseEntity<Map<String, Object>> checkDependenciesHealth() {
        Map<String, Object> health = new HashMap<>();

        // Check Booking Service health
        boolean bookingServiceHealthy = bookingServiceClient.isServiceHealthy();
        String bookingServiceUrl = bookingServiceClient.getServiceUrl();

        Map<String, Object> bookingServiceStatus = new HashMap<>();
        bookingServiceStatus.put("healthy", bookingServiceHealthy);
        bookingServiceStatus.put("info", bookingServiceUrl);

        health.put("bookingService", bookingServiceStatus);
        health.put("overallStatus", bookingServiceHealthy ? "UP" : "DOWN");
        health.put("timestamp", java.time.LocalDateTime.now());

        HttpStatus status = bookingServiceHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    @GetMapping("/debug/booking-service")
    @Operation(summary = "Get Booking Service connection info",
            description = "Returns detailed information about Booking Service connection for debugging")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connection info retrieved")
    })
    public ResponseEntity<Map<String, Object>> getBookingServiceDebugInfo() {
        Map<String, Object> debugInfo = new HashMap<>();

        debugInfo.put("serviceUrl", bookingServiceClient.getServiceUrl());
        debugInfo.put("isHealthy", bookingServiceClient.isServiceHealthy());
        debugInfo.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(debugInfo);
    }
}