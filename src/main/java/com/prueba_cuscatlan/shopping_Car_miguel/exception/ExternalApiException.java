package com.prueba_cuscatlan.shopping_Car_miguel.exception;

public class ExternalApiException extends RuntimeException {

    private final int statusCode;

    public ExternalApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ExternalApiException(String message) {
        super(message);
        this.statusCode = 503;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
