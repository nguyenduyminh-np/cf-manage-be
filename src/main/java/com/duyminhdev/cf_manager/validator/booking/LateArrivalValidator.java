package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LateArrivalValidator extends AbstractBookingValidationRule {

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
        LocalDateTime arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("expectedArriveTime is required");
        }

        LocalDateTime checkInAt = context.resolveCheckInOrNow();
        if (checkInAt.isAfter(arriveAt.plusMinutes(30))) {
            fail("late check-in over 30 minutes is not allowed; use no-show flow first");
        }
    }
}
