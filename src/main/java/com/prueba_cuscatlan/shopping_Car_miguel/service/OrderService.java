package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CheckoutRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.UpdateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    /** Converts an existing cart into a confirmed order and clears the cart. */
    OrderResponse checkout(CheckoutRequest request);

    Page<OrderResponse> findAll(Pageable pageable);

    OrderResponse findById(Long id);

    OrderResponse update(Long id, UpdateOrderRequest request);

    void cancel(Long id);
}
