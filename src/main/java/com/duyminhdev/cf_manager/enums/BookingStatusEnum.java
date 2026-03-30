package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum BookingStatusEnum implements EnumCodeSupport {
    PENDING("PENDING_CONFIRMATION", "Chờ xác nhận"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    CANCELLED("CANCELLED", "Đã huỷ"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    EXPIRED("EXPIRED", "Đã hết hạn");

    private final String code;
    private final String label;

    public static BookingStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking status code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
