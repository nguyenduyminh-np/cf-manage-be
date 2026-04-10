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
public class WalkInGuardValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

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
            fail("tableId is required for walk-in guard");
        }

        LocalDateTime now = context.getNow();
        boolean hasNearConfirmed = tableBookingRepository.existsActiveBookingOnTableInWindowAndStatuses(
                tableId,
                now,
                now.plusMinutes(30),
                List.of(BookingStatusEnum.CONFIRMED.getCode()),
                context.resolveBookingId()
        );

        if (hasNearConfirmed) {
            fail("table is reserved by a confirmed booking in the next 30 minutes");
        }
    }
}
