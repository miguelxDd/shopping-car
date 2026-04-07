package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartItemRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.service.CartService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Shopping cart management per user")
@RestController
@RequestMapping(Constants.CARTS_PATH)
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get or create a cart for a user")
    @GetMapping("/{userId}")
    ResponseEntity<CartResponse> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @Operation(summary = "Add an item to the cart")
    @PostMapping("/{userId}/items")
    ResponseEntity<CartResponse> addItem(@PathVariable String userId,
                                        @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @Operation(summary = "Update the quantity of a cart item (0 = remove)")
    @PatchMapping("/{userId}/items/{productId}")
    ResponseEntity<CartResponse> updateQuantity(@PathVariable String userId,
                                                @PathVariable Long productId,
                                                @RequestParam @Min(0) Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, productId, quantity));
    }

    @Operation(summary = "Remove a specific item from the cart")
    @DeleteMapping("/{userId}/items/{productId}")
    ResponseEntity<CartResponse> removeItem(@PathVariable String userId,
                                            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @Operation(summary = "Clear all items from the cart")
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
