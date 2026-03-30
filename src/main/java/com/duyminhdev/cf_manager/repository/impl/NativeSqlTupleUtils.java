package main.java.com.duyminhdev.cf_manager.repository.impl;

import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    public static LocalDateTime getLocalDateTime(Tuple tuple, String alias) {
        Object value = tuple.get(alias);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
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
