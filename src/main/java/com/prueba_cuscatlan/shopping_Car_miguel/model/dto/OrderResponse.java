package com.prueba_cuscatlan.shopping_Car_miguel.model.dto;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private CustomerResponse customer;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal total;
    private List<OrderDetailResponse> details;
    private OrderPaymentResponse payment;
}
