package com.duyminhdev.cf_manager.constant;

public final class BookingLockConstant {

    private BookingLockConstant() {
    }

    public static final String TABLE_LOCK_KEY_PREFIX = "lock:table:";
    public static final String ERROR_CODE_TABLE_LOCK_BUSY = "BOOKING_TABLE_LOCK_BUSY";

    public static final long TABLE_LOCK_WAIT_SECONDS = 2L;
    public static final long TABLE_LOCK_LEASE_SECONDS = 10L;
}
