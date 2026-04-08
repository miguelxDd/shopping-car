package com.prueba_cuscatlan.shopping_Car_miguel.service.payment.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentResult;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(PaymentContext context) {
        log.info("Cash payment orderId={} → APPROVED [thread={}]",
                context.getOrderId(), Thread.currentThread().getName());

        return PaymentResult.builder()
                .approved(true)
                .transactionId("CASH-" + UUID.randomUUID())
                .message("Cash payment registered successfully")
                .build();
    }
}
