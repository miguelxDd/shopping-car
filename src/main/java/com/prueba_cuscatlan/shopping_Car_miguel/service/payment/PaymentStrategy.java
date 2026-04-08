package com.prueba_cuscatlan.shopping_Car_miguel.service.payment;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;


public interface PaymentStrategy {

    PaymentMethod getSupportedMethod();

    PaymentResult process(PaymentContext context);
}
