package com.duyminhdev.cf_manager.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresInSeconds;
    private String message;

    @JsonProperty("user_info")
    private UserInfo userInfo;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {
        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("phone_number")
        private String phoneNumber;
    }
}
