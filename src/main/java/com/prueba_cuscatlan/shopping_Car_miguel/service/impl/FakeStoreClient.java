package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.config.FakeStoreProperties;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FakeStoreClient {

    private final RestTemplate restTemplate;
    private final FakeStoreProperties properties;

    @CircuitBreaker(name = "fakestore")
    @Retry(name = "fakestore")
    public List<ExternalProductDTO> findAll() {
        return restTemplate.exchange(
                properties.getBaseUrl(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ExternalProductDTO>>() {
                }).getBody();
    }

    @CircuitBreaker(name = "fakestore")
    @Retry(name = "fakestore")
    public ExternalProductDTO findById(Long id) {
        return restTemplate.getForObject(
                properties.getBaseUrl() + "/{id}",
                ExternalProductDTO.class, id);
    }

    @CircuitBreaker(name = "fakestore")
    @Retry(name = "fakestore")
    public List<String> findCategories() {
        return restTemplate.exchange(
                properties.getBaseUrl() + "/categories",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>() {
                }).getBody();
    }

    @CircuitBreaker(name = "fakestore")
    @Retry(name = "fakestore")
    public List<ExternalProductDTO> findByCategory(String category) {
        return restTemplate.exchange(
                properties.getBaseUrl() + "/category/{category}",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ExternalProductDTO>>() {
                },
                category).getBody();
    }
}
