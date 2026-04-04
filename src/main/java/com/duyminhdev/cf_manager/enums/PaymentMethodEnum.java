package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentMethodEnum implements EnumCodeSupport {
    CASH("CASH", "Tiền mặt", true, 1),
    BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản", true, 2);

    private final String code;
    private final String label;
    private final boolean active;
    private final int displayOrder;

    public static PaymentMethodEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment method code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    public boolean isCash() {
        return this == CASH;
    }

    public boolean isBankTransfer() {
        return this == BANK_TRANSFER;
    }
}
