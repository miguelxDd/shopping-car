package com.prueba_cuscatlan.shopping_Car_miguel.service.impl;

import com.prueba_cuscatlan.shopping_Car_miguel.config.JwtProperties;
import com.prueba_cuscatlan.shopping_Car_miguel.exception.BadRequestException;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.AuthResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.LoginRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.auth.RegisterRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.entity.Customer;
import com.prueba_cuscatlan.shopping_Car_miguel.repository.CustomerRepository;
import com.prueba_cuscatlan.shopping_Car_miguel.security.JwtService;
import com.prueba_cuscatlan.shopping_Car_miguel.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository  customerRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtService          jwtService;
    private final JwtProperties       jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        Customer customer = Customer.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .build();

        customerRepository.save(customer);
        log.info("New customer registered: {}", customer.getUsername());

        String token = jwtService.generateToken(customer);
        return buildResponse(token, customer);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Customer customer = (Customer) auth.getPrincipal();
        String token = jwtService.generateToken(customer);
        log.info("Customer logged in: {}", customer.getUsername());
        return buildResponse(token, customer);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(String token, Customer customer) {
        return AuthResponse.builder()
                .token(token)
                .username(customer.getUsername())
                .email(customer.getEmail())
                .expiresInMs(jwtProperties.getExpirationMs())
                .build();
    }
}
