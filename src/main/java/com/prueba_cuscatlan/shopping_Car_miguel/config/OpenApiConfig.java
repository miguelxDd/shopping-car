package com.prueba_cuscatlan.shopping_Car_miguel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shopping Cart API")
                        .description("REST API for shopping cart management — prueba técnica Cuscatlán")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Miguel Amaya")
                                .email("miguelxdxp94@gmail.com")));
    }
}
