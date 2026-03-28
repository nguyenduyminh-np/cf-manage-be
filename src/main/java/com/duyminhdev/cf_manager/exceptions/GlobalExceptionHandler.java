package com.duyminhdev.cf_manager.exceptions;

import com.duyminhdev.cf_manager.dto.base.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            Object details,
            HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .code(code)
                        .message(message)
                        .details(details)
                        .path(req.getRequestURI())
                        .build());
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> invalidData(
            InvalidDataException ex,
            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "INVALID_DATA",
                ex.getMessage(),
                null,
                req);
    }

    @ExceptionHandler(DuplicatedUsernameException.class)
    public ResponseEntity<ErrorResponse> duplicatedUsername(
            DuplicatedUsernameException ex,
            HttpServletRequest req) {
        return build(HttpStatus.CONFLICT,
                "DUPLICATED_USERNAME",
                ex.getMessage(),
                null,
                req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest req) {
        // Log the actual exception so we don't lose it
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                ex.getMessage(),
                req);
    }
}
