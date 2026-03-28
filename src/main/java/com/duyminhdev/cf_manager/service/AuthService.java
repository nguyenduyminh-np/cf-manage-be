package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.AuthResponse;

public interface AuthService {

    AuthResponse login(String username, String rawPassword);

    AuthResponse register(String username, String rawPassword, String fullName);

    AuthResponse refresh(String refreshToken);

    AuthResponse logout(String refreshToken);

    AuthResponse registerAdmin(String username, String rawPassword, String fullName);
}
