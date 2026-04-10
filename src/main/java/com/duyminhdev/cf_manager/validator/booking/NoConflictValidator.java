package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NoConflictValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CREATE_BOOKING
                || useCase == BookingValidationUseCase.CONFIRM_BOOKING
                || useCase == BookingValidationUseCase.LATE_ARRIVAL_WALK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("tableId is required for conflict check");
        }

        LocalDateTime start = context.resolveExpectedArriveTime();
        LocalDateTime end = context.resolveExpectedCheckOut();
        if (start == null || end == null) {
            fail("expectedArriveTime and expectedCheckOut are required for conflict check");
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
            fail("time slot conflicts with an existing booking on the same table");
        }
    }
}
