package com.duyminhdev.cf_manager.exceptions;

import com.duyminhdev.cf_manager.dto.base.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String code = (String) request.getAttribute("auth_error_code");
        String message = (String) request.getAttribute("auth_error_message");

        if (code == null || code.isBlank()) {
            code = "AUTH_UNAUTHORIZED";
        }
        if (message == null || message.isBlank()) {
            message = "Yêu cầu chưa được xác thực hoặc token không hợp lệ";
        }

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), body);
    }
}
