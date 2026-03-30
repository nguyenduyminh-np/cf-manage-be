package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TableStatusEnum implements EnumCodeSupport {
    AVAILABLE("AVAILABLE", "Bàn trống"),
    OCCUPIED("OCCUPIED", "Đang sử dụng"),
    BOOKED("BOOKED", "Đã đặt");

    private final String code;
    private final String label;

    public static TableStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid table status code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isOccupied() {
        return this == OCCUPIED;
    }

    public boolean isBooked() {
        return this == BOOKED;
    }
}
