package com.duyminhdev.cf_manager.exceptions;

import com.duyminhdev.cf_manager.dto.base.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;

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
}
