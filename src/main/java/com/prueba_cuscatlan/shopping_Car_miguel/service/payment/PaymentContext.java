package com.prueba_cuscatlan.shopping_Car_miguel.service.payment;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentContext {

    private final Long orderId;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
}
