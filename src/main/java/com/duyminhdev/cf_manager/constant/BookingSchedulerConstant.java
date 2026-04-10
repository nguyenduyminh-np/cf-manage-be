package com.duyminhdev.cf_manager.constant;

import java.time.Duration;

public final class BookingSchedulerConstant {

    private BookingSchedulerConstant() {
    }

    public static final long RESERVE_WINDOW_MINUTES = 30L;
    public static final long NO_SHOW_GRACE_MINUTES = 30L;
    public static final long NO_ORDER_WARNING_MINUTES = 10L;
    public static final long NO_ORDER_CANCEL_MINUTES = 20L;
    public static final long CHECKOUT_REMINDER_MINUTES = 15L;

    public static final Duration NOTIFICATION_DEDUP_TTL = Duration.ofMinutes(45);

    public static final String TOPIC_TABLE_STATUS = "/topic/table-status";
    public static final String TOPIC_BOOKING_UPDATES = "/topic/booking-updates";
    public static final String TOPIC_TABLE_ALERTS = "/topic/table-alerts";
    public static final String TOPIC_DEPOSIT_EVENTS = "/topic/deposit-events";

    public static final String DEDUP_KEY_RESERVED_PREFIX = "booking:scheduler:reserve:";
    public static final String DEDUP_KEY_NO_SHOW_PREFIX = "booking:scheduler:no-show:";
    public static final String DEDUP_KEY_OCCUPIED_CONFLICT_PREFIX = "booking:scheduler:occupied-conflict:";
    public static final String DEDUP_KEY_NO_ORDER_WARN_PREFIX = "booking:scheduler:no-order-warn:";
    public static final String DEDUP_KEY_NO_ORDER_CANCEL_PREFIX = "booking:scheduler:no-order-cancel:";
    public static final String DEDUP_KEY_CHECKOUT_REMINDER_PREFIX = "booking:scheduler:checkout-reminder:";

    public static final String DEDUP_KEY_MUTATION_BOOKING_PREFIX = "booking:mutation:booking:";
    public static final String DEDUP_KEY_MUTATION_TABLE_PREFIX = "booking:mutation:table:";
    public static final String DEDUP_KEY_MUTATION_DEPOSIT_PREFIX = "booking:mutation:deposit:";
}
