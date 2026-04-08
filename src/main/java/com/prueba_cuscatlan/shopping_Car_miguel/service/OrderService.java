package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse create(String userId, OrderRequest request);

    /** Converts an existing cart into a confirmed order and clears the cart. */
    OrderResponse checkout(String userId);

    Page<OrderResponse> findAllByUser(String userId, Pageable pageable);

    OrderResponse findById(String userId, Long id);

    OrderResponse update(String userId, Long id, UpdateOrderRequest request);

    void cancel(String userId, Long id);
}
