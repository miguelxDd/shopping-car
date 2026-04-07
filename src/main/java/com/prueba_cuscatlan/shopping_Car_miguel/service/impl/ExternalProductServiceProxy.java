package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ExternalApiException;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.ResourceNotFoundException;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ExternalProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class ExternalProductServiceProxy implements ExternalProductService {

    private final FakeStoreClient fakeStoreClient;

    @Override
    public List<ExternalProductDTO> findAll() {
        log.info("Fetching all products from external API");
        return execute(fakeStoreClient::findAll, "Failed to retrieve products from external API");
    }

    @Override
    public ExternalProductDTO findById(Long id) {
        log.info("Fetching external product with id={}", id);
        ExternalProductDTO product = execute(
                () -> fakeStoreClient.findById(id),
                "Failed to retrieve product id=" + id + " from external API");
        if (product == null) {
            throw new ResourceNotFoundException("External product not found with id: " + id);
        }
        return product;
    }

    @Override
    public List<String> findCategories() {
        log.info("Fetching product categories from external API");
        return execute(fakeStoreClient::findCategories, "Failed to retrieve categories from external API");
    }

    @Override
    public List<ExternalProductDTO> findByCategory(String category) {
        log.info("Fetching external products for category='{}'", category);
        return execute(
                () -> fakeStoreClient.findByCategory(category),
                "Failed to retrieve products for category '" + category + "' from external API");
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T call();
    }

    private <T> T execute(ApiCall<T> call, String errorMessage) {
        try {
            return call.call();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException(errorMessage);
            }
            log.warn("External API client error: {} {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(errorMessage, ex.getStatusCode().value());
        } catch (HttpServerErrorException ex) {
            log.error("External API server error: {} {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException("External API is currently unavailable", ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            log.error("External API timeout or connection refused: {}", ex.getMessage());
            throw new ExternalApiException("External API is unreachable (timeout or connection refused)");
        } catch (Exception ex) {
            log.error("Unexpected error calling external API: {}", ex.getMessage());
            throw new ExternalApiException(errorMessage);
        }
    }
}
