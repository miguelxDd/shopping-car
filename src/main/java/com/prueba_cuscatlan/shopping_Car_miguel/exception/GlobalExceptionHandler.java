package com.prueba_cuscatlan.shopping_Car_miguel.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null));
        }

        @ExceptionHandler(BadRequestException.class)
        ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null));
        }

        @ExceptionHandler(BusinessException.class)
        ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Business Error", ex.getMessage(), null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                List<String> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .toList();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Validation Failed", "Invalid request body",
                                                details));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
                List<String> details = ex.getConstraintViolations()
                                .stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .toList();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Validation Failed", "Constraint violation",
                                                details));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Malformed Request",
                                                "Request body is missing or unreadable", null));
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Missing Parameter",
                                                "Required parameter '" + ex.getParameterName() + "' is missing", null));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                String detail = "Parameter '" + ex.getName() + "' should be of type "
                                + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildError(HttpStatus.BAD_REQUEST, "Type Mismatch", detail, null));
        }

        @ExceptionHandler(ExternalApiException.class)
        ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException ex) {
                HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
                if (status == null)
                        status = HttpStatus.SERVICE_UNAVAILABLE;
                return ResponseEntity.status(status)
                                .body(buildError(status, "External API Error", ex.getMessage(), null));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                                                ex.getMessage(), null));
        }

        private ErrorResponse buildError(HttpStatus status, String error, String message, List<String> details) {
                return ErrorResponse.builder()
                                .status(status.value())
                                .error(error)
                                .message(message)
                                .details(details)
                                .timestamp(LocalDateTime.now())
                                .build();
        }
}
