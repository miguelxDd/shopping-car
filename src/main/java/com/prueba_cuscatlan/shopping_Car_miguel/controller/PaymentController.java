package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ErrorResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.service.PaymentService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Payments", description = "Async payment simulation with Strategy pattern")
@RestController
@RequestMapping(Constants.PAYMENTS_PATH)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
        summary = "Submit a payment (async)",
        description = """
            Returns 202 Accepted immediately with status=PENDING. \
            The payment is processed on a dedicated thread pool. \
            Poll GET /payments/{id} until status changes to COMPLETED or FAILED. \
            Provide Idempotency-Key header to safely retry without double-charging.\
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Payment accepted — processing asynchronously",
            headers = @Header(name = "Location", description = "URL to poll for payment status",
                schema = @Schema(type = "string", example = "/api/v1/payments/42")),
            content = @Content(schema = @Schema(implementation = OrderPaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Amount mismatch, order already paid, or validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<OrderPaymentResponse> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderPaymentRequest request) {

        OrderPaymentResponse response = paymentService.processPayment(idempotencyKey, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.accepted()          // 202
                .header("Location", location.toString())
                .body(response);
    }

    @Operation(summary = "Poll payment status", description = "Returns PENDING, COMPLETED, or FAILED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found",
            content = @Content(schema = @Schema(implementation = OrderPaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<OrderPaymentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }
}
