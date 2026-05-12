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
    /** Cửa sổ thời gian cảnh báo: nếu đang tạo booking mà bàn đó có đơn CONFIRMED trong vòng 2 tiếng tới → cảnh báo vàng */
    public static final long UPCOMING_BOOKING_WARN_HOURS = 2L;
    /** Nhắc overdue mỗi 2 phút bằng cách dùng time-window key (epoch / 120) */
    public static final long CHECKOUT_OVERDUE_REPEAT_SECONDS = 120L;

    public static final Duration NOTIFICATION_DEDUP_TTL = Duration.ofMinutes(45);

    public static final String TOPIC_TABLE_STATUS    = "/topic/table-status";
    public static final String TOPIC_BOOKING_UPDATES = "/topic/booking-updates";
    public static final String TOPIC_TABLE_ALERTS    = "/topic/table-alerts";
    public static final String TOPIC_DEPOSIT_EVENTS  = "/topic/deposit-events";
    /** Topic riêng cho bếp: nhận order mới, món sẵn sàng, order bị hủy */
    public static final String TOPIC_KITCHEN_ORDERS  = "/topic/kitchen-orders";

    public static final String DEDUP_KEY_RESERVED_PREFIX = "booking:scheduler:reserve:";
    public static final String DEDUP_KEY_NO_SHOW_PREFIX = "booking:scheduler:no-show:";
    public static final String DEDUP_KEY_OCCUPIED_CONFLICT_PREFIX = "booking:scheduler:occupied-conflict:";
    public static final String DEDUP_KEY_NO_ORDER_WARN_PREFIX = "booking:scheduler:no-order-warn:";
    public static final String DEDUP_KEY_NO_ORDER_CANCEL_PREFIX = "booking:scheduler:no-order-cancel:";
    public static final String DEDUP_KEY_CHECKOUT_REMINDER_PREFIX = "booking:scheduler:checkout-reminder:";
    /** Key chứa time-window (epoch/120) → tự hết hạn key cũ sau 45 phút, gửi lại mỗi 2 phút */
    public static final String DEDUP_KEY_CHECKOUT_OVERDUE_PREFIX  = "booking:scheduler:checkout-overdue:";

    public static final String DEDUP_KEY_MUTATION_BOOKING_PREFIX  = "booking:mutation:booking:";
    public static final String DEDUP_KEY_MUTATION_TABLE_PREFIX    = "booking:mutation:table:";
    public static final String DEDUP_KEY_MUTATION_DEPOSIT_PREFIX  = "booking:mutation:deposit:";
    /** Dedup key cho cảnh báo vàng: bàn có đơn CONFIRMED sắp đến trong 2 tiếng */
    public static final String DEDUP_KEY_UPCOMING_BOOKING_WARN_PREFIX = "booking:warn:upcoming:create:";

    // Kitchen order dedup keys
    public static final String DEDUP_KEY_ORDER_CREATED_PREFIX     = "order:kitchen:created:";
    public static final String DEDUP_KEY_ORDER_READY_PREFIX       = "order:kitchen:ready:";
    public static final String DEDUP_KEY_ORDER_CANCELLED_PREFIX   = "order:kitchen:cancelled:";

    // Payment dedup keys
    public static final String DEDUP_KEY_PAYMENT_COMPLETED_PREFIX = "payment:completed:";
    public static final String DEDUP_KEY_PAYMENT_FAILED_PREFIX    = "payment:failed:";

    // Order processing timeout (scheduler)
    public static final long ORDER_PROCESSING_TIMEOUT_MINUTES = 30L;
    public static final String DEDUP_KEY_ORDER_TIMEOUT_PREFIX     = "order:kitchen:timeout:";
}
