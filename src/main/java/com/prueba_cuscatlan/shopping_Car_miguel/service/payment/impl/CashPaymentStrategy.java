package com.prueba_cuscatlan.shopping_Car_miguel.service.payment.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentResult;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
    }

    @Async
    @Override
    public CompletableFuture<PaymentResult> process(PaymentContext context) {
        // Cash: no network call, always approved immediately
        log.info("Cash payment for orderId={} amount={} → APPROVED [thread={}]",
                context.getOrder().getId(), context.getAmount(), Thread.currentThread().getName());

        return CompletableFuture.completedFuture(PaymentResult.builder()
                .approved(true)
                .transactionId("CASH-" + UUID.randomUUID())
                .message("Cash payment registered successfully")
                .build());
    }
}
