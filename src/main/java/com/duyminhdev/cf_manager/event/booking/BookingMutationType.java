package com.duyminhdev.cf_manager.event.booking;

public enum BookingMutationType {
    CREATE,
    UPDATE,
    UPDATE_STATUS,
    CONFIRM,
    CHECK_IN,
    CHECK_OUT,
    CANCEL,
    EXPIRE,
    EXTEND,
    WALK_IN,
    LATE_ARRIVAL_WALK_IN,
    CANCEL_NO_ORDER_TIMEOUT,
    DEPOSIT
}
