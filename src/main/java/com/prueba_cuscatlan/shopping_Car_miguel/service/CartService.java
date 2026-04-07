package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartItemRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartResponse;


public interface CartService {

    CartResponse getCartByUserId(String userId);

    CartResponse addItem(String userId, CartItemRequest request);

    CartResponse updateItemQuantity(String userId, Long productId, Integer quantity);

    CartResponse removeItem(String userId, Long productId);

    void clearCart(String userId);
}
