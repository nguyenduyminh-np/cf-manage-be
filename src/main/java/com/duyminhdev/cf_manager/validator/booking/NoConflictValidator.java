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
public class NoConflictValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_01_02_12_NO_CONFLICT";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CREATE_BOOKING
                || useCase == BookingValidationUseCase.CONFIRM_BOOKING
                || useCase == BookingValidationUseCase.WALK_IN_BOOKING
                || useCase == BookingValidationUseCase.LATE_ARRIVAL_WALK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("Mã bàn (tableId) là bắt buộc để kiểm tra xung đột lịch đặt");
        }

        Instant start = context.resolveExpectedArriveTime();
        Instant end = context.resolveExpectedCheckOut();
        if (start == null || end == null) {
            fail("Thời gian đến và trả bàn là bắt buộc để kiểm tra xung đột lịch đặt");
        }

        Integer excludeBookingId = context.resolveBookingId();

        boolean hasConflict = tableBookingRepository.existsConflictBookingOnTable(
                tableId,
                start,
                end,
                List.of(
                        BookingStatusEnum.CONFIRMED.getCode(),
                        BookingStatusEnum.CHECKED_IN.getCode()
                ),
                excludeBookingId
        );

        if (hasConflict) {
            fail("Khoảng thời gian đặt bàn bị xung đột với lịch đặt khác trên cùng bàn này");
        }
    }
}
