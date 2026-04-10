package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DurationValidator extends AbstractBookingValidationRule {

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
        LocalDateTime arriveAt = context.resolveExpectedArriveTime();
        LocalDateTime checkOutAt = context.resolveExpectedCheckOut();

        if (arriveAt == null) {
            fail("expectedArriveTime is required");
        }

        if (checkOutAt == null) {
            fail("expectedCheckOut is required");
        }

        if (!checkOutAt.isAfter(arriveAt)) {
            fail("expectedCheckOut must be greater than expectedArriveTime");
        }
    }
}
