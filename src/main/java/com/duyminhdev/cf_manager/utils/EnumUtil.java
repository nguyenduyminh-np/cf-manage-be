package com.duyminhdev.cf_manager.utils;

import java.util.Arrays;

public class EnumUtil {
    private EnumUtil() {
    }

    public static <E extends Enum<E> & EnumCodeSupport> E fromCode(Class<E> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid code '" + code + "' for enum " + enumClass.getSimpleName()
                ));
    }

    public static <E extends Enum<E> & EnumCodeSupport> boolean equalsCode(E enumValue, String code) {
        return enumValue != null && code != null && enumValue.getCode().equalsIgnoreCase(code);
    }
}
