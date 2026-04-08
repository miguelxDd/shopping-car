package com.prueba_cuscatlan.shopping_Car_miguel.service.payment.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentContext;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentResult;
import com.prueba_cuscatlan.shopping_Car_miguel.service.payment.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.random.RandomGenerator;

@Slf4j
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final double SUCCESS_RATE = 0.90;

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult process(PaymentContext context) {
        simulateNetworkDelay(); // runs on paymentExecutor thread, never blocks HTTP thread

        boolean approved = RandomGenerator.getDefault().nextDouble() < SUCCESS_RATE;
        log.info("CreditCard payment orderId={} → {} [thread={}]",
                context.getOrderId(), approved ? "APPROVED" : "DECLINED",
                Thread.currentThread().getName());

        return PaymentResult.builder()
                .approved(approved)
                .transactionId(approved ? "CC-" + UUID.randomUUID() : null)
                .message(approved ? "Credit card payment approved" : "Credit card declined by issuer")
                .build();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1000 + RandomGenerator.getDefault().nextLong(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
