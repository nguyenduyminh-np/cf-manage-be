package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalkInGuardValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_09_16_WALK_IN_GUARD";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.WALK_IN_BOOKING
                || useCase == BookingValidationUseCase.LATE_ARRIVAL_WALK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("Mã bàn (tableId) là bắt buộc để gác lịch walk-in");
        }

        Instant start = context.resolveExpectedArriveTime();
        Instant end = context.resolveExpectedCheckOut();
        if (start == null || end == null) {
            fail("Thời gian đến và trả bàn là bắt buộc để gác lịch walk-in");
        }

        boolean hasConflict = tableBookingRepository.existsConflictBookingOnTable(
                tableId,
                start,
                end,
                List.of(
                        BookingStatusEnum.CONFIRMED.getCode(),
                        BookingStatusEnum.CHECKED_IN.getCode()
                ),
                context.resolveBookingId()
        );

        if (hasConflict) {
            fail("Khoảng thời gian walk-in xung đột với đặt bàn đang hoạt động trên cùng bàn này");
        }
    }
}
