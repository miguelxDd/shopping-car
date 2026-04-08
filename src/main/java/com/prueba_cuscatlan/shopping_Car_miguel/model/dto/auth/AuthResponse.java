package com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private long expiresInMs;
}
