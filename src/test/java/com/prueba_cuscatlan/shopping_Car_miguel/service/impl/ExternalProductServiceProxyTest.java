package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ExternalApiException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalProductServiceProxyTest {

    @Mock
    FakeStoreClient fakeStoreClient;

    @InjectMocks
    ExternalProductServiceProxy proxy;

    private ExternalProductDTO product;

    @BeforeEach
    void setUp() {
        product = ExternalProductDTO.builder()
                .id(1L).title("Fjallraven Backpack")
                .price(new BigDecimal("109.95"))
                .category("men's clothing")
                .build();
    }

    @Test
    @DisplayName("findAll delegates to FakeStoreClient and returns products")
    void findAll_returnsProducts_fromClient() {
        when(fakeStoreClient.findAll()).thenReturn(List.of(product));

        List<ExternalProductDTO> result = proxy.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Fjallraven Backpack");
        verify(fakeStoreClient).findAll();
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when API returns 404")
    void findById_throwsResourceNotFound_on404() {
        when(fakeStoreClient.findById(99L))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> proxy.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById throws ExternalApiException on timeout")
    void findById_throwsExternalApiException_onTimeout() {
        when(fakeStoreClient.findById(1L))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        assertThatThrownBy(() -> proxy.findById(1L))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("unreachable");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when client returns null")
    void findById_throwsResourceNotFound_whenResultIsNull() {
        when(fakeStoreClient.findById(1L)).thenReturn(null);

        assertThatThrownBy(() -> proxy.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    @DisplayName("findCategories delegates to FakeStoreClient")
    void findCategories_returnsCategories() {
        when(fakeStoreClient.findCategories()).thenReturn(List.of("electronics", "jewelery"));

        List<String> result = proxy.findCategories();

        assertThat(result).containsExactly("electronics", "jewelery");
    }

    @Test
    @DisplayName("findByCategory throws ExternalApiException on 5xx error")
    void findByCategory_throwsExternalApiException_on5xx() {
        when(fakeStoreClient.findByCategory("electronics"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null));

        assertThatThrownBy(() -> proxy.findByCategory("electronics"))
                .isInstanceOf(ExternalApiException.class);
    }
}
