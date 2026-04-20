package com.duyminhdev.cf_manager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    @NotBlank(message = "Lỗi refresh")
    private String refreshToken;
}
