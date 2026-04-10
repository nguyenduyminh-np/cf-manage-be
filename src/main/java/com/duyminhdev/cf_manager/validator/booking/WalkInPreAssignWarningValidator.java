package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WalkInPreAssignWarningValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

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
            fail("tableId is required for walk-in warning check");
        }

        boolean hasUpcomingConfirmed = tableBookingRepository.existsActiveBookingOnTableInWindowAndStatuses(
                tableId,
                context.getNow().plusMinutes(30),
                context.getNow().plusHours(2),
                List.of(BookingStatusEnum.CONFIRMED.getCode()),
                context.resolveBookingId()
        );

        if (hasUpcomingConfirmed) {
            warn(context, "table has confirmed booking in the next 2 hours; notify walk-in customer before assigning");
        }
    }
}
