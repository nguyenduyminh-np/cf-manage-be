package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.auth.LoginRequest;
import com.duyminhdev.cf_manager.dto.auth.LogoutRequest;
import com.duyminhdev.cf_manager.dto.auth.RefreshRequest;
import com.duyminhdev.cf_manager.dto.auth.RegisterRequest;
import com.duyminhdev.cf_manager.dto.base.AuthResponse;
import com.duyminhdev.cf_manager.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req.getUsername(), req.getPassword(), req.getFullName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.getUsername(), req.getPassword());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.getRefreshToken());
    }

    @PostMapping("/logout")
    public AuthResponse logout(@Valid @RequestBody LogoutRequest req) {
        return authService.logout(req.getRefreshToken());
    }

    @PostMapping("/register-admin")
    public AuthResponse registerAdmin(@Valid @RequestBody RegisterRequest req) {
        return authService.registerAdmin(req.getUsername(), req.getPassword(), req.getFullName());
    }
}
