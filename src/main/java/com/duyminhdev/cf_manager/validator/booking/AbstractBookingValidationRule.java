package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.constant.BookingStateMachineConstant;
import com.duyminhdev.cf_manager.exceptions.BookingStateTransitionException;

public abstract class AbstractBookingValidationRule implements BookingValidationRule {

    protected void fail(String message) {
        throw new BookingStateTransitionException(
                BookingStateMachineConstant.PREFIX_RULE_VIOLATION + message
        );
    }

    protected void warn(BookingValidationContext context, String message) {
        context.addWarning(message);
    }
}
