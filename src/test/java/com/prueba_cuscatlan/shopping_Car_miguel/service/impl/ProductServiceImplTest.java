package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.mapper.ProductMapper;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ProductRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ProductResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Product;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    ProductMapper productMapper;

    @InjectMocks
    ProductServiceImpl productService;

    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).name("Laptop").description("Gaming laptop")
                .price(new BigDecimal("999.99")).stock(10)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L).name("Laptop").description("Gaming laptop")
                .price(new BigDecimal("999.99")).stock(10)
                .build();
    }

    @Test
    @DisplayName("findAll returns paginated products")
    void findAll_returnsPaginatedProducts() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        Page<ProductResponse> result = productService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findById returns product when found")
    void findById_returnsProduct_whenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when not found")
    void findById_throwsException_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create saves and returns product")
    void create_savesAndReturnsProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("Laptop").description("Gaming laptop")
                .price(new BigDecimal("999.99")).stock(10)
                .build();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException when product does not exist")
    void delete_throwsException_whenProductNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete calls deleteById when product exists")
    void delete_callsDeleteById_whenProductExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}
