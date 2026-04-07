package com.prueba_cuscatlan.shopping_Car_miguel.service.payment;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class PaymentResult {

    private final boolean approved;
    private final String transactionId;
    private final String message;
}
