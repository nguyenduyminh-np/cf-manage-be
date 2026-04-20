package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.constant.BookingStateMachineConstant;
import com.duyminhdev.cf_manager.exceptions.BookingStateTransitionException;

public abstract class AbstractBookingValidationRule implements BookingValidationRule {

    protected void fail(String message) {
        throw new BookingStateTransitionException(
                BookingStateMachineConstant.PREFIX_RULE_VIOLATION + formatTaggedMessage(message)
        );
    }

    protected void warn(BookingValidationContext context, String message) {
        context.addWarning(formatTaggedMessage(message));
    }

    protected String ruleTag() {
        return "RULE_UNSPECIFIED";
    }

    private String formatTaggedMessage(String message) {
        String normalized = message == null ? "" : message.trim();
        return "[" + ruleTag() + "][" + getClass().getSimpleName() + "] " + normalized;
    }
}
