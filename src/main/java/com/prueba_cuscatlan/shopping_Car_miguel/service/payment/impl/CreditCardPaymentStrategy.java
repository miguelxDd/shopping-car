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
import java.util.random.RandomGenerator;

@Slf4j
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final double SUCCESS_RATE = 0.90;

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Async
    @Override
    public CompletableFuture<PaymentResult> process(PaymentContext context) {
        simulateNetworkDelay();

        boolean approved = RandomGenerator.getDefault().nextDouble() < SUCCESS_RATE;
        log.info("CreditCard payment for orderId={} amount={} → {} [thread={}]",
                context.getOrder().getId(), context.getAmount(),
                approved ? "APPROVED" : "DECLINED", Thread.currentThread().getName());

        return CompletableFuture.completedFuture(PaymentResult.builder()
                .approved(approved)
                .transactionId(approved ? "CC-" + UUID.randomUUID() : null)
                .message(approved ? "Credit card payment approved" : "Credit card declined by issuer")
                .build());
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1000 + RandomGenerator.getDefault().nextLong(1000)); // 1-2 s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
