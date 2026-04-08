package com.prueba_cuscatlan.shopping_Car_miguel.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderDetailRequest> items;
}
