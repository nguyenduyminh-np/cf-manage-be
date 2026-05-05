package com.duyminhdev.cf_manager.utils.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser rule-based dùng Regex để trích xuất thông tin bàn trống từ tin nhắn tiếng Việt.
 * Hỗ trợ các mẫu câu phổ biến của khách hàng.
 */
public final class TableQueryParser {

    // ---- Tầng ----
    private static final Pattern FLOOR_PATTERN = Pattern.compile(
            "tầng\\s*(\\d+)|tầng\\s*trệt|tầng\\s*1",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // ---- Ngày ----
    private static final Pattern DATE_TOMORROW = Pattern.compile(
            "ngày\\s*mai|hôm\\s*sau|mai", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DATE_TODAY = Pattern.compile(
            "hôm\\s*nay|tối\\s*nay|trưa\\s*nay|sáng\\s*nay", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DATE_EXPLICIT = Pattern.compile(
            "ngày\\s*(\\d{1,2})[/\\-](\\d{1,2})(?:[/\\-](\\d{2,4}))?");

    // ---- Giờ ----
    // Khớp: 19h, 7h30, 19:00, 7:30, 7 giờ, 7 giờ 30 phút
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{1,2})\\s*[h:]\\s*(\\d{0,2})(?:\\s*phút)?|(\\d{1,2})\\s*giờ(?:\\s*(\\d{1,2})\\s*phút)?");

    // ---- Từ .. đến ----
    // Tìm cặp giờ bắt đầu - kết thúc: "từ 19h đến 21h", "19h - 21h"
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile(
            "(?:từ\\s*)?(\\d{1,2})\\s*[h:]\\s*(\\d{0,2})\\s*(?:đến|tới|\\-)\\s*(\\d{1,2})\\s*[h:]\\s*(\\d{0,2})");

    private TableQueryParser() {}

    /**
     * Phân tích tin nhắn và trả về tham số truy vấn bàn trống.
     * Trả về null nếu không thể trích xuất giờ.
     */
    public static TableQueryParams parse(String message) {
        if (message == null || message.isBlank()) return null;

        Integer floor     = extractFloor(message);
        LocalDate date    = extractDate(message);
        LocalTime[] times = extractTimeRange(message);

        if (times == null) return null;

        return TableQueryParams.builder()
                .floor(floor)
                .date(date)
                .startTime(times[0])
                .endTime(times[1])
                .build();
    }

    // ---- private helpers ----

    private static Integer extractFloor(String msg) {
        Matcher m = FLOOR_PATTERN.matcher(msg);
        if (!m.find()) return null;
        String matched = m.group(0).toLowerCase().replaceAll("\\s+", "");
        if (matched.contains("trệt") || matched.equals("tầng1")) return 1;
        String digits = m.group(1);
        return digits != null ? Integer.parseInt(digits) : null;
    }

    private static LocalDate extractDate(String msg) {
        if (DATE_TOMORROW.matcher(msg).find()) {
            return LocalDate.now(ChatTimeUtils.CAFE_ZONE).plusDays(1);
        }
        if (DATE_TODAY.matcher(msg).find()) {
            return LocalDate.now(ChatTimeUtils.CAFE_ZONE);
        }
        Matcher m = DATE_EXPLICIT.matcher(msg);
        if (m.find()) {
            int day   = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year  = m.group(3) != null
                    ? (m.group(3).length() == 2 ? 2000 + Integer.parseInt(m.group(3)) : Integer.parseInt(m.group(3)))
                    : LocalDate.now(ChatTimeUtils.CAFE_ZONE).getYear();
            try {
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                return null;
            }
        }
        // Mặc định hôm nay
        return LocalDate.now(ChatTimeUtils.CAFE_ZONE);
    }

    private static LocalTime[] extractTimeRange(String msg) {
        // Thử khớp "từ Xh đến Yh" trước
        Matcher rangeMatcher = TIME_RANGE_PATTERN.matcher(msg);
        if (rangeMatcher.find()) {
            LocalTime start = parseTime(rangeMatcher.group(1), rangeMatcher.group(2));
            LocalTime end   = parseTime(rangeMatcher.group(3), rangeMatcher.group(4));
            if (start != null && end != null) {
                return new LocalTime[]{start, end};
            }
        }

        // Chỉ có 1 giờ → giả định duration 2 tiếng
        Matcher timeMatcher = TIME_PATTERN.matcher(msg);
        if (timeMatcher.find()) {
            String hourStr   = timeMatcher.group(1) != null ? timeMatcher.group(1) : timeMatcher.group(3);
            String minStr    = timeMatcher.group(2) != null ? timeMatcher.group(2) : timeMatcher.group(4);
            LocalTime start  = parseTime(hourStr, minStr);
            if (start != null) {
                LocalTime end = start.plusHours(2);
                // Nếu cuối ngày, cap lại 22:00
                if (end.isAfter(LocalTime.of(22, 0))) end = LocalTime.of(22, 0);
                return new LocalTime[]{start, end};
            }
        }

        return null;
    }

    private static LocalTime parseTime(String hourStr, String minStr) {
        if (hourStr == null || hourStr.isBlank()) return null;
        try {
            int hour = Integer.parseInt(hourStr.trim());
            int min  = (minStr != null && !minStr.isBlank()) ? Integer.parseInt(minStr.trim()) : 0;
            return LocalTime.of(hour, min);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Inner DTO ----

    @Getter
    @Builder
    public static class TableQueryParams {
        private final Integer   floor;      // null = tất cả tầng
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public Instant startInstant() {
            return ChatTimeUtils.toInstant(startTime, date);
        }

        public Instant endInstant() {
            return ChatTimeUtils.toInstant(endTime, date);
        }
    }
}
