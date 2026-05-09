package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Loại giảm giá của voucher:
 * - PERCENT : Giảm theo phần trăm (kết hợp với maxDiscount để giới hạn)
 * - FIXED   : Giảm theo số tiền cố định
 */
@Getter
@RequiredArgsConstructor
public enum DiscountTypeEnum implements EnumCodeSupport {

    PERCENT("PERCENT", "Giảm theo %"),
    FIXED("FIXED", "Giảm tiền cố định");

    private final String code;
    private final String label;

    public static DiscountTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid discount type: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values()).anyMatch(e -> e.code.equalsIgnoreCase(code));
    }
}
