package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalkInPreAssignWarningValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_16_WALK_IN_PRE_ASSIGN_WARNING";
    }

    @Override
    public int order() {
        return 30;
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
            fail("Mã bàn (tableId) là bắt buộc để kiểm tra cảnh báo walk-in");
        }

        boolean hasUpcomingConfirmed = tableBookingRepository.existsActiveBookingOnTableInWindowAndStatuses(
                tableId,
                context.getNow().plus(Duration.ofMinutes(30)),
                context.getNow().plus(Duration.ofHours(2)),
                List.of(BookingStatusEnum.CONFIRMED.getCode()),
                context.resolveBookingId()
        );

        if (hasUpcomingConfirmed) {
            warn(context, "Bàn có đặt bàn đã xác nhận trong 2 tiếng tới; nhân viên cần thông báo cho khách walk-in trước khi xếp bàn");
        }
    }
}
