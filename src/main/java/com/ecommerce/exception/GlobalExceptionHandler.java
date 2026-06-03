package com.ecommerce.exception;

import com.ecommerce.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(exception.getMessage()));
    }

    @ExceptionHandler({BadRequestException.class, ConstraintViolationException.class, InvalidOrderStatusException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of(exception.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, InsufficientStockException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), List.of(exception.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException exception) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", List.of("Invalid credentials"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException exception) {
        return build(HttpStatus.FORBIDDEN, "Access denied", List.of("Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", List.of("Unexpected error"));
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, List<String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(false, message, errors, Instant.now()));
    }
}
