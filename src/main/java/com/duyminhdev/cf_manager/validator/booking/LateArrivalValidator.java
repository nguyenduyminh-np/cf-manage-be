package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class LateArrivalValidator extends AbstractBookingValidationRule {

    @Override
    protected String ruleTag() {
        return "RULE_04_12_LATE_ARRIVAL";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CHECK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Instant arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("Thời gian đến dự kiến (expectedArriveTime) không được để trống");
        }

        Instant checkInAt = context.resolveCheckInOrNow();
        if (checkInAt.isAfter(arriveAt.plus(Duration.ofMinutes(30)))) {
            fail("Check-in trễ quá 30 phút không được phép; vui lòng thực hiện luồng no-show trước");
        }
    }
}
