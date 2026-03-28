package com.duyminhdev.cf_manager.dto.records;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String issuer,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays,
        String privateKey,
        String publicKey
) {
}
