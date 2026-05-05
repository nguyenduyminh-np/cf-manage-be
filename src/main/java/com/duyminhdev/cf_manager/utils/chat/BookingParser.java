package com.duyminhdev.cf_manager.utils.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser rule-based trích xuất thông tin đặt bàn từ tin nhắn tiếng Việt.
 * Kết quả được dùng để gọi TableBookingService.create().
 */
public final class BookingParser {

    // ---- Tên khách ----
    // Khớp: "cho anh/chị/em <Tên>", "tên: <Tên>", "khách: <Tên>"
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?:cho\\s+|tên\\s*[:\\s]+|khách\\s*[:\\s]+)(?:anh|chị|em|bạn|ông|bà|cô|chú)?\\s*([A-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠ][a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđA-ZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ]+(?:\\s+[a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđA-ZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ]+)*)",
            Pattern.UNICODE_CASE);

    // ---- SĐT ----
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:sđt|số\\s*điện\\s*thoại|phone|tel)[\\s:]*([0-9]{9,11})|\\b(0[0-9]{9})\\b");

    // ---- Số người ----
    private static final Pattern GUEST_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:người|khách|chỗ|ghế|pax)");

    // ---- Tầng ----
    private static final Pattern FLOOR_PATTERN = Pattern.compile(
            "tầng\\s*(\\d+)|tầng\\s*trệt", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private BookingParser() {}

    /**
     * Phân tích tin nhắn đặt bàn, trả về null nếu thiếu thông tin tối thiểu (giờ).
     */
    public static BookingParseResult parse(String message) {
        if (message == null || message.isBlank()) return null;

        String customerName = extractName(message);
        String phone        = extractPhone(message);
        Integer guestCount  = extractGuestCount(message);
        Integer floor       = extractFloor(message);

        // Dùng lại TableQueryParser để lấy giờ
        TableQueryParser.TableQueryParams timeParams = TableQueryParser.parse(message);
        if (timeParams == null || timeParams.getStartTime() == null) return null;

        return BookingParseResult.builder()
                .customerName(customerName)
                .phoneNumber(phone)
                .guestCount(guestCount)
                .preferredFloor(floor)
                .expectedArriveTime(timeParams.startInstant())
                .expectedCheckOut(timeParams.endInstant())
                .build();
    }

    private static String extractName(String msg) {
        Matcher m = NAME_PATTERN.matcher(msg);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String extractPhone(String msg) {
        Matcher m = PHONE_PATTERN.matcher(msg);
        if (!m.find()) return null;
        String g1 = m.group(1);
        String g2 = m.group(2);
        return g1 != null ? g1 : g2;
    }

    private static Integer extractGuestCount(String msg) {
        Matcher m = GUEST_PATTERN.matcher(msg);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static Integer extractFloor(String msg) {
        Matcher m = FLOOR_PATTERN.matcher(msg);
        if (!m.find()) return null;
        String group = m.group(1);
        return group != null ? Integer.parseInt(group) : 1; // "tầng trệt" = 1
    }

    // ---- Result DTO ----

    @Getter
    @Builder
    public static class BookingParseResult {
        private final String  customerName;
        private final String  phoneNumber;
        private final Integer guestCount;
        private final Integer preferredFloor;
        private final Instant expectedArriveTime;
        private final Instant expectedCheckOut;

        public boolean hasRequiredInfo() {
            return expectedArriveTime != null && expectedCheckOut != null;
        }
    }
}
