package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ErrorResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartItemRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.service.CartService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Cart", description = "Shopping cart management for the authenticated user")
@RestController
@RequestMapping(Constants.CARTS_PATH)
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get or create the cart for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Cart returned (created if it did not exist)", content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @GetMapping
    ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCartByUserId(principal.getName()));
    }

    @Operation(summary = "Add an item to the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added, updated cart returned", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. quantity < 1)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found in external catalog", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/items")
    ResponseEntity<CartResponse> addItem(Principal principal,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(principal.getName(), request));
    }

    @Operation(summary = "Update the quantity of a cart item (0 = remove)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantity updated, updated cart returned", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "quantity must be >= 0", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cart or item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/items/{productId}")
    ResponseEntity<CartResponse> updateQuantity(Principal principal,
            @PathVariable Long productId,
            @RequestParam @Min(0) Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getName(), productId, quantity));
    }

    @Operation(summary = "Remove a specific item from the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed, updated cart returned", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cart or item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/items/{productId}")
    ResponseEntity<CartResponse> removeItem(Principal principal,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getName(), productId));
    }

    @Operation(summary = "Clear all items from the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart cleared"),
            @ApiResponse(responseCode = "404", description = "Cart not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    ResponseEntity<Void> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
