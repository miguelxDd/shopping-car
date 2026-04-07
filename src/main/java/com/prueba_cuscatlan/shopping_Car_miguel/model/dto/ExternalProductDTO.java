package com.prueba_cuscatlan.shopping_Car_miguel.model.dto;

import lombok.*;

import java.math.BigDecimal;

// DTO that maps the response from the external FakeStore API
//No JPA entity this data lives only in the external system

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProductDTO {

    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String category;
    private String image;
}
