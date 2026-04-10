package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdvanceBookingValidator extends AbstractBookingValidationRule {

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
        LocalDateTime arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("expectedArriveTime is required");
        }

        LocalDateTime now = context.getNow() != null ? context.getNow() : LocalDateTime.now();
        if (arriveAt.isBefore(now.plusHours(2))) {
            fail("expectedArriveTime must be at least 2 hours after now");
        }
    }
}
