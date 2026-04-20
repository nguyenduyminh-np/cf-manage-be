package com.duyminhdev.cf_manager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "Lỗi refresh token")
    private String refreshToken;
}
