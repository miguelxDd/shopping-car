package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ErrorResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.service.OrderService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

@Tag(name = "Orders", description = "Order lifecycle management")
@RestController
@RequestMapping(Constants.ORDERS_PATH)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Checkout — convert cart into a confirmed order", description = "Fetches current prices from FakeStore, creates the order, and clears the cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created from cart", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cart is empty", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer or cart not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "FakeStore API unavailable", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/checkout")
    ResponseEntity<OrderResponse> checkout(Principal principal) {
        OrderResponse order = orderService.checkout(principal.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(Constants.ORDERS_PATH + "/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @Operation(summary = "Create a new order (prices fetched from external API)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or empty item list", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer or product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "External product API unavailable (circuit open)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<OrderResponse> create(Principal principal,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.create(principal.getName(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(Constants.ORDERS_PATH + "/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @Operation(summary = "List the authenticated user's orders (paginated)")
    @ApiResponse(responseCode = "200", description = "Paginated order list")
    @GetMapping
    ResponseEntity<Page<OrderResponse>> findAll(Principal principal,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllByUser(principal.getName(), pageable));
    }

    @Operation(summary = "Get an order by ID (must belong to authenticated user)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Order does not belong to authenticated user", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<OrderResponse> findById(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(principal.getName(), id));
    }

    @Operation(summary = "Update order status or items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Order does not belong to authenticated user", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<OrderResponse> update(Principal principal, @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(principal.getName(), id, request));
    }

    @Operation(summary = "Cancel an order")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order cancelled"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled (already paid or delivered)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Order does not belong to authenticated user", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> cancel(Principal principal, @PathVariable Long id) {
        orderService.cancel(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
