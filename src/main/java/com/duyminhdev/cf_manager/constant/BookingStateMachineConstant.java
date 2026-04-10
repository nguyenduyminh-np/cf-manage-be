package com.duyminhdev.cf_manager.constant;

public final class BookingStateMachineConstant {

    private BookingStateMachineConstant() {
    }

    public static final String ERROR_CODE_STATE_TRANSITION_INVALID = "BOOKING_STATE_TRANSITION_INVALID";

    public static final String PREFIX_NOT_ALLOWED = "BOOKING_TRANSITION_NOT_ALLOWED: ";
    public static final String PREFIX_GUARD_FAILED = "BOOKING_TRANSITION_GUARD_FAILED: ";
    public static final String PREFIX_RULE_VIOLATION = "BOOKING_TRANSITION_RULE_VIOLATION: ";
}
