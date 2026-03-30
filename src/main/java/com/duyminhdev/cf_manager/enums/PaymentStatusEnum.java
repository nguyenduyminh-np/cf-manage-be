package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentStatusEnum implements EnumCodeSupport {
    PENDING("PENDING", "Chờ thanh toán"),
    PAID("PAID", "Đã thanh toán"),
    CANCEL("CANCEL", "Đã huỷ");

    private final String code;
    private final String label;

    public static PaymentStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment status code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPaid() {
        return this == PAID;
    }
}
