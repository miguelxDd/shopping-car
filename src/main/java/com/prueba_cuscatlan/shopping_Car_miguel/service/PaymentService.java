package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;

public interface PaymentService {

    /** Creates a PENDING payment and triggers async processing. Returns immediately. */
    OrderPaymentResponse processPayment(String idempotencyKey, OrderPaymentRequest request);

    /** Polls the current status of a payment. */
    OrderPaymentResponse findById(Long id);
}
