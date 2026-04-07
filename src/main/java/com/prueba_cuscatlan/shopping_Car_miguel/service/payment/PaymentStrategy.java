package com.prueba_cuscatlan.shopping_Car_miguel.service.payment;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;

import java.util.concurrent.CompletableFuture;

public interface PaymentStrategy {

    PaymentMethod getSupportedMethod();

    CompletableFuture<PaymentResult> process(PaymentContext context);
}
