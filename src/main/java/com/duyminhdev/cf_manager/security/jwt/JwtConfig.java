package com.duyminhdev.cf_manager.security.jwt;

import com.duyminhdev.cf_manager.dto.records.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
}
