package com.duyminhdev.cf_manager.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Giới hạn truy cập chỉ dành cho role ADMIN (Quản lý).
 * <p>
 * Áp dụng cho các chức năng quản trị nhạy cảm mà STAFF (Nhân viên)
 * không được phép thực hiện, ví dụ:
 * - Quản lý tài khoản
 * - Dashboard, báo cáo tổng quan
 * - Quản lý kho, nhà cung cấp, nguyên liệu
 * - CRUD voucher
 * - AI Chat
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@adminOnlyAuthorization.check(authentication)")
public @interface AdminOnlyAccess {
}
