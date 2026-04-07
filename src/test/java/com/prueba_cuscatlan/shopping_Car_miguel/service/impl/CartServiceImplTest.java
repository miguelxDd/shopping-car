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
import com.prueba_cuscatlan.shopping_Car_miguel.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

        @Mock
        CartRepository cartRepository;
        @Mock
        CartItemRepository cartItemRepository;
        @Mock
        ProductService productService;
        @Mock
        CartMapper cartMapper;

        @InjectMocks
        CartServiceImpl cartService;

        private Cart cart;
        private ProductResponse productResponse;
        private CartResponse cartResponse;

        @BeforeEach
        void setUp() {
                cart = Cart.builder()
                                .id(1L).userId("user-1")
                                .items(new ArrayList<>())
                                .build();

                productResponse = ProductResponse.builder()
                                .id(10L).name("Keyboard").price(new BigDecimal("49.99")).stock(5)
                                .build();

                cartResponse = CartResponse.builder()
                                .id(1L).userId("user-1")
                                .total(BigDecimal.ZERO)
                                .build();
        }

        @Test
        @DisplayName("getCartByUserId creates cart when user has none")
        void getCartByUserId_createsCart_whenNoneExists() {
                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.empty());
                when(cartRepository.save(any(Cart.class))).thenReturn(cart);
                when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

                CartResponse result = cartService.getCartByUserId("user-1");

                assertThat(result.getUserId()).isEqualTo("user-1");
                verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("addItem adds product snapshot to cart")
        void addItem_addsSnapshotToCart_whenStockSufficient() {
                CartItemRequest request = CartItemRequest.builder()
                                .productId(10L).quantity(2).build();

                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
                when(productService.findById(10L)).thenReturn(productResponse);
                when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
                when(cartRepository.save(cart)).thenReturn(cart);
                when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

                cartService.addItem("user-1", request);

                // CartItem was added with snapshot fields — no FK to Product entity
                assertThat(cart.getItems()).hasSize(1);
                CartItem added = cart.getItems().get(0);
                assertThat(added.getProductId()).isEqualTo(10L);
                assertThat(added.getProductName()).isEqualTo("Keyboard");
                assertThat(added.getUnitPrice()).isEqualByComparingTo("49.99");
        }

        @Test
        @DisplayName("addItem throws BusinessException when stock is insufficient")
        void addItem_throwsBusinessException_whenStockInsufficient() {
                CartItemRequest request = CartItemRequest.builder()
                                .productId(10L).quantity(100).build(); // stock = 5

                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
                when(productService.findById(10L)).thenReturn(productResponse);

                assertThatThrownBy(() -> cartService.addItem("user-1", request))
                                .isInstanceOf(BusinessException.class)
                                .hasMessageContaining("stock");
        }

        @Test
        @DisplayName("addItem increments quantity when item already exists")
        void addItem_incrementsQuantity_whenItemAlreadyInCart() {
                CartItemRequest request = CartItemRequest.builder()
                                .productId(10L).quantity(1).build();

                CartItem existing = CartItem.builder()
                                .id(5L).productId(10L).productName("Keyboard")
                                .quantity(2).unitPrice(new BigDecimal("49.99"))
                                .cart(cart).build();
                cart.getItems().add(existing);

                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
                when(productService.findById(10L)).thenReturn(productResponse);
                when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
                when(cartRepository.save(cart)).thenReturn(cart);
                when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

                cartService.addItem("user-1", request);

                assertThat(existing.getQuantity()).isEqualTo(3); // 2 + 1
                assertThat(cart.getItems()).hasSize(1); // no duplicate item
        }

        @Test
        @DisplayName("removeItem removes the correct product from cart")
        void removeItem_removesProduct_fromCart() {
                CartItem item = CartItem.builder()
                                .id(5L).productId(10L).productName("Keyboard")
                                .quantity(1).unitPrice(new BigDecimal("49.99"))
                                .cart(cart).build();
                cart.getItems().add(item);

                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
                when(cartRepository.save(cart)).thenReturn(cart);
                when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

                cartService.removeItem("user-1", 10L);

                assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("clearCart empties the cart")
        void clearCart_emptiesAllItems() {
                CartItem item = CartItem.builder()
                                .id(5L).productId(10L).productName("Keyboard")
                                .quantity(1).unitPrice(new BigDecimal("49.99"))
                                .cart(cart).build();
                cart.getItems().add(item);

                when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
                when(cartRepository.save(cart)).thenReturn(cart);

                cartService.clearCart("user-1");

                assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("removeItem throws ResourceNotFoundException when cart not found")
        void removeItem_throwsException_whenCartNotFound() {
                when(cartRepository.findByUserId("ghost")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> cartService.removeItem("ghost", 10L))
                                .isInstanceOf(ResourceNotFoundException.class);
        }
}
