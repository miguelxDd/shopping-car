package com.prueba_cuscatlan.shopping_Car_miguel.model.dto;

import com.prueba_cuscatlan.shopping_Car_miguel.model.enums.OrderStatus;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderRequest {

    // null: keep existing status
    private OrderStatus status;

    // null: keep existing items; non-null: replace all items and recalculate total
    @Valid
    private List<OrderDetailRequest> items;
}
