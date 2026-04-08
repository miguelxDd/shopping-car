package com.prueba_cuscatlan.shopping_Car_miguel.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotBlank(message = "User ID is required")
    private String userId;
}
