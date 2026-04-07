package com.prueba_cuscatlan.shopping_Car_miguel.mapper;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartItemResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Cart;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

// No ProductMapper dependency --- CartItem carries its own snapshot
@Component
public class CartMapper {

        public CartResponse toResponse(Cart cart) {
                List<CartItemResponse> itemResponses = cart.getItems().stream()
                                .map(this::toItemResponse)
                                .toList();

                BigDecimal total = itemResponses.stream()
                                .map(CartItemResponse::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return CartResponse.builder()
                                .id(cart.getId())
                                .userId(cart.getUserId())
                                .items(itemResponses)
                                .total(total)
                                .createdAt(cart.getCreatedAt())
                                .updatedAt(cart.getUpdatedAt())
                                .build();
        }

        private CartItemResponse toItemResponse(CartItem item) {
                BigDecimal subtotal = item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));

                return CartItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(subtotal)
                                .build();
        }
}
