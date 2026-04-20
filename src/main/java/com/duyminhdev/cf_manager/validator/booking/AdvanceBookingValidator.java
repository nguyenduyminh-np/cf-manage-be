package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdvanceBookingValidator extends AbstractBookingValidationRule {

    @Override
    protected String ruleTag() {
        return "RULE_01_ADVANCE_BOOKING";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CREATE_BOOKING;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Instant arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("Thời gian đến dự kiến (expectedArriveTime) không được để trống");
        }

        // Temporarily disable rule: expectedArriveTime must be at least 2 hours after current time.
        // Instant now = context.getNow() != null ? context.getNow() : Instant.now();
        // if (arriveAt.isBefore(now.plus(Duration.ofHours(2)))) {
        //     fail("Thời gian đặt bàn phải sớm hơn thời điểm hiện tại ít nhất 2 tiếng");
        // }
    }
}
