package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositValidator extends AbstractBookingValidationRule {

    @Override
    protected String ruleTag() {
        return "RULE_02_DEPOSIT_REQUIRED";
    }

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
            fail("Không tìm thấy thông tin đặt bàn");
        }

        BigDecimal amount = booking.getDepositAmount();
        boolean requiresDeposit = amount != null && amount.compareTo(BigDecimal.ZERO) > 0;

        if (requiresDeposit && !Boolean.TRUE.equals(booking.getDepositPaid())) {
            fail("Không thể xác nhận đặt bàn khi chưa thanh toán tiền cọc");
        }
    }
}
