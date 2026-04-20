package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class ExpectedArriveTimeWindowValidator extends AbstractBookingValidationRule {

    private static final ZoneId CAFE_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final LocalTime OPEN_TIME = LocalTime.of(6, 0);
    private static final LocalTime LAST_ALLOWED_ARRIVE_TIME = LocalTime.of(20, 0);

    @Override
    protected String ruleTag() {
        return "RULE_01_EXPECTED_ARRIVE_WINDOW";
    }

    @Override
    public int order() {
        return 15;
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

        LocalTime localArriveTime = arriveAt.atZone(CAFE_ZONE_ID).toLocalTime();
        if (localArriveTime.isBefore(OPEN_TIME) || localArriveTime.isAfter(LAST_ALLOWED_ARRIVE_TIME)) {
            fail("Thời gian expectedArriveTime phải nằm trong khoảng 06:00 - 20:00 (giờ quán)");
        }
    }
}
