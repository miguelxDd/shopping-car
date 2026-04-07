package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.BusinessException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.mapper.CartMapper;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartItemRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CartResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ProductResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Cart;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.CartItem;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CartItemRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CartRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.service.CartService;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCartByUserId(String userId) {
        return cartMapper.toResponse(findOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(String userId, CartItemRequest request) {
        Cart cart = findOrCreateCart(userId);

        ProductResponse product = productService.findById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }

        cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                        () -> cart.getItems().add(CartItem.builder()
                                .cart(cart)
                                .productId(product.getId())
                                .productName(product.getName()) // snapshot — no FK to Product
                                .quantity(request.getQuantity())
                                .unitPrice(product.getPrice())
                                .build()));

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userId, Long productId, Integer quantity) {
        Cart cart = findCartOrThrow(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found for product id: " + productId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userId, Long productId) {
        Cart cart = findCartOrThrow(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = findCartOrThrow(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart findOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
    }

    private Cart findCartOrThrow(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
    }
}
