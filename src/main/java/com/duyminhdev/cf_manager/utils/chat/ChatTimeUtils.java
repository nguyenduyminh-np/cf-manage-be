package com.duyminhdev.cf_manager.utils.chat;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích xử lý thời gian cho chatbot.
 * Toàn bộ thời gian nhập vào được hiểu theo múi giờ Asia/Ho_Chi_Minh,
 * sau đó convert sang UTC Instant để khớp với dữ liệu trong DB.
 */
public final class ChatTimeUtils {

    public static final ZoneId CAFE_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy").withZone(CAFE_ZONE);

    private static final DateTimeFormatter TIME_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(CAFE_ZONE);

    private ChatTimeUtils() {}

    /**
     * Chuyển LocalTime + LocalDate từ múi giờ quán sang UTC Instant.
     */
    public static Instant toInstant(LocalTime time, LocalDate date) {
        LocalDate resolvedDate = date != null ? date : LocalDate.now(CAFE_ZONE);
        return LocalDateTime.of(resolvedDate, time)
                .atZone(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Tạo Instant từ giờ và phút (ngày hôm nay theo múi giờ quán).
     */
    public static Instant todayAt(int hour, int minute) {
        return LocalDate.now(CAFE_ZONE)
                .atTime(hour, minute)
                .atZone(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Tạo Instant từ giờ và phút vào ngày mai theo múi giờ quán.
     */
    public static Instant tomorrowAt(int hour, int minute) {
        return LocalDate.now(CAFE_ZONE)
                .plusDays(1)
                .atTime(hour, minute)
                .atZone(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Format Instant thành chuỗi "HH:mm ngày dd/MM/yyyy" theo múi giờ quán.
     */
    public static String format(Instant instant) {
        if (instant == null) return "N/A";
        return DISPLAY_FORMATTER.format(instant);
    }

    /**
     * Format Instant chỉ lấy phần giờ:phút theo múi giờ quán.
     */
    public static String formatTimeOnly(Instant instant) {
        if (instant == null) return "N/A";
        return TIME_ONLY_FORMATTER.format(instant);
    }

    /**
     * Lấy đầu ngày hôm nay (00:00:00) theo múi giờ quán, convert sang UTC.
     */
    public static Instant startOfToday() {
        return LocalDate.now(CAFE_ZONE)
                .atStartOfDay(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Lấy cuối ngày hôm nay (23:59:59) theo múi giờ quán, convert sang UTC.
     */
    public static Instant endOfToday() {
        return LocalDate.now(CAFE_ZONE)
                .atTime(23, 59, 59)
                .atZone(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Lấy đầu tuần này (thứ Hai 00:00:00) theo múi giờ quán.
     */
    public static Instant startOfThisWeek() {
        return LocalDate.now(CAFE_ZONE)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(CAFE_ZONE)
                .toInstant();
    }

    /**
     * Lấy đầu tháng này (ngày 1, 00:00:00) theo múi giờ quán.
     */
    public static Instant startOfThisMonth() {
        return LocalDate.now(CAFE_ZONE)
                .with(java.time.temporal.TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay(CAFE_ZONE)
                .toInstant();
    }
}
