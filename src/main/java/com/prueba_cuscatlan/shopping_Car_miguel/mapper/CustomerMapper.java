package com.prueba_cuscatlan.shopping_Car_miguel.mapper;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.CustomerResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .build();
    }
}
