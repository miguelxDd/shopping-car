package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.service.OrderService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "Order lifecycle management")
@RestController
@RequestMapping(Constants.ORDERS_PATH)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order (prices fetched from external API)")
    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @Operation(summary = "List all orders (paginated)")
    @GetMapping
    ResponseEntity<Page<OrderResponse>> findAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @Operation(summary = "Get an order by ID")
    @GetMapping("/{id}")
    ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @Operation(summary = "Update order status or items")
    @PutMapping("/{id}")
    ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                         @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @Operation(summary = "Cancel an order")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
