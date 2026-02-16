package com.celebration.api;

import com.celebration.service.AppException;
import com.celebration.service.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String code, String message, String traceId) {
    }

    @ExceptionHandler(AppException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        return org.springframework.http.ResponseEntity.status(ex.getHttpStatus())
                .body(new ErrorResponse(ex.getCode().name(), ex.getMessage(), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("validation error");
        return new ErrorResponse(ErrorCode.VALIDATION_ERROR.name(), message, traceId());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest request) {
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.name(), "unexpected error", traceId());
    }

    private String traceId() {
        return "trace-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
