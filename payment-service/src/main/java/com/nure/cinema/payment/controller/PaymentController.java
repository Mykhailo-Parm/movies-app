package com.nure.cinema.payment.controller;

import com.nure.cinema.payment.dto.*;
import com.nure.cinema.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Service", description = "API for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
}