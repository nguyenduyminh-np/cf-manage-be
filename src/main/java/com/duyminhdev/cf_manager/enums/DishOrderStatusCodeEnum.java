package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DishOrderStatusCodeEnum implements EnumCodeSupport {

    PROCESSING("PROCESSING", "Đang xử lý"),
    DONE("DONE", "Hoàn thành"),
    CANCEL("CANCEL", "Đã huỷ"),
    PAID ("PAID", "Đã thanh toán");

    private final String code;
    private final String label;

    public static DishOrderStatusCodeEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid dish order status code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }
}
