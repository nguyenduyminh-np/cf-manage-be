package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.AuthResponse;

public interface AuthService {

    /**
     * Xác thực tài khoản và phát hành access token/refresh token mới.
     */
    AuthResponse login(String username, String rawPassword);

    /**
     * Đăng ký tài khoản người dùng mới với role mặc định hệ thống.
     */
    AuthResponse register(String username, String rawPassword, String fullName);

    /**
     * Làm mới access token bằng refresh token còn hiệu lực.
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Thu hồi refresh token hiện tại để đăng xuất phiên đăng nhập.
     */
    AuthResponse logout(String refreshToken);

    /**
     * Đăng ký tài khoản quản trị viên.
     */
    AuthResponse registerAdmin(String username, String rawPassword, String fullName);
}
