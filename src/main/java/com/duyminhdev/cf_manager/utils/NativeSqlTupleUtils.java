package com.duyminhdev.cf_manager.utils;

import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class NativeSqlTupleUtils {

    private NativeSqlTupleUtils() {
    }

    public static String getString(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        return value != null ? value.toString() : null;
    }

    public static Integer getInteger(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        return value != null ? ((Number) value).intValue() : null;
    }

    public static Long getLong(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        return value != null ? ((Number) value).longValue() : null;
    }

    public static BigDecimal getBigDecimal(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }

    public static Boolean getBoolean(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public static Instant getInstant(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return null;
    }

    public static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
