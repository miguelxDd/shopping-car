package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.OrderPaymentResponse;

public interface PaymentService {

    OrderPaymentResponse processPayment(String idempotencyKey, OrderPaymentRequest request);
}
