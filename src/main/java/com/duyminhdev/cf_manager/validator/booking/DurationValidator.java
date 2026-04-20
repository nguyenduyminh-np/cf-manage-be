package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class DurationValidator extends AbstractBookingValidationRule {

    @Override
    protected String ruleTag() {
        return "RULE_01_DURATION";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CREATE_BOOKING;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Instant arriveAt = context.resolveExpectedArriveTime();
        Instant checkOutAt = context.resolveExpectedCheckOut();

        if (arriveAt == null) {
            fail("Thời gian đến dự kiến (expectedArriveTime) không được để trống");
        }

        if (checkOutAt == null) {
            fail("Thời gian trả bàn dự kiến (expectedCheckOut) không được để trống");
        }

        if (!checkOutAt.isAfter(arriveAt)) {
            fail("Thời gian trả bàn phải sau thời gian đến");
        }
    }
}
