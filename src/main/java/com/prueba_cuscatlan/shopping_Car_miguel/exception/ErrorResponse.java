package com.prueba_cuscatlan.shopping_Car_miguel.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
}
