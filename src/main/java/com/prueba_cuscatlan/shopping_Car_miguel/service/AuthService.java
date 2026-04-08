package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.AuthResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.LoginRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
