package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositValidator extends AbstractBookingValidationRule {

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CONFIRM_BOOKING;
    }

    @Override
    public void validate(BookingValidationContext context) {
        TableBooking booking = context.getBooking();
        if (booking == null) {
            fail("booking is required");
        }

        BigDecimal amount = booking.getDepositAmount();
        boolean requiresDeposit = amount != null && amount.compareTo(BigDecimal.ZERO) > 0;

        if (requiresDeposit && !Boolean.TRUE.equals(booking.getDepositPaid())) {
            fail("cannot confirm booking when deposit is unpaid");
        }
    }
}
