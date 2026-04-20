package com.duyminhdev.cf_manager.exceptions;

import com.duyminhdev.cf_manager.constant.BookingStateMachineConstant;
import com.duyminhdev.cf_manager.constant.BookingLockConstant;
import com.duyminhdev.cf_manager.dto.base.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        @ExceptionHandler(BookingStateTransitionException.class)
        public ResponseEntity<ErrorResponse> bookingStateTransition(
                        BookingStateTransitionException ex,
                        HttpServletRequest req) {
                return build(HttpStatus.BAD_REQUEST,
                                BookingStateMachineConstant.ERROR_CODE_STATE_TRANSITION_INVALID,
                                ex.getMessage(),
                                null,
                                req);
        }

        @ExceptionHandler(BookingLockException.class)
        public ResponseEntity<ErrorResponse> bookingLock(
                        BookingLockException ex,
                        HttpServletRequest req) {
                return build(HttpStatus.CONFLICT,
                                BookingLockConstant.ERROR_CODE_TABLE_LOCK_BUSY,
                                ex.getMessage(),
                                null,
                                req);
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", fieldErrors);

        return build(HttpStatus.BAD_REQUEST,
                "INVALID_DATA",
                "Dữ liệu đầu vào không hợp lệ",
                details,
                req);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(
            BindException ex,
            HttpServletRequest req
    ) {
        List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", fieldErrors);

        return build(HttpStatus.BAD_REQUEST,
                "INVALID_DATA",
                "Dữ liệu đầu vào không hợp lệ",
                details,
                req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest req
    ) {
        List<Map<String, Object>> violations = ex.getConstraintViolations().stream()
                .map(item -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("field", item.getPropertyPath() != null ? item.getPropertyPath().toString() : null);
                    detail.put("message", item.getMessage());
                    detail.put("rejectedValue", item.getInvalidValue());
                    return detail;
                })
                .toList();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", violations);

        return build(HttpStatus.BAD_REQUEST,
                "INVALID_DATA",
                "Dữ liệu đầu vào không hợp lệ",
                details,
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
                "Lỗi hệ thống nội bộ, vui lòng thử lại sau",
                ex.getMessage(),
                req);
    }

        private Map<String, Object> toFieldErrorDetail(FieldError fieldError) {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("field", fieldError.getField());
                detail.put("message", fieldError.getDefaultMessage());
                detail.put("rejectedValue", fieldError.getRejectedValue());
                return detail;
        }
}
