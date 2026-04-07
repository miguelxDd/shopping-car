package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.service.PaymentService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments", description = "Payment simulation with Strategy pattern")
@RestController
@RequestMapping(Constants.PAYMENTS_PATH)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process a payment for an order",
               description = "Simulates a payment (90% success rate). Idempotent via Idempotency-Key header.")
    @PostMapping
    ResponseEntity<OrderPaymentResponse> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(idempotencyKey, request));
    }
}
