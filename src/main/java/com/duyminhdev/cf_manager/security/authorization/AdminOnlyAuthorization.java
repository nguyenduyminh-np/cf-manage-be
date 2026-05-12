package com.duyminhdev.cf_manager.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("adminOnlyAuthorization")
public class AdminOnlyAuthorization {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    /**
     * Chỉ cho phép ADMIN (Quản lý) truy cập.
     * Dùng cho các chức năng quản trị nhạy cảm:
     *  - Quản lý tài khoản nhân viên
     *  - Dashboard tổng quan
     *  - Quản lý kho hàng, nhà cung cấp
     *  - Quản lý voucher (CRUD)
     *  - Hóa đơn, báo cáo
     *  - AI Chat (dữ liệu nhạy cảm)
     */
    public boolean check(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(ADMIN_AUTHORITY::equals);
    }
}
