package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EarlyArrivalValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CHECK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        LocalDateTime arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("expectedArriveTime is required");
        }

        LocalDateTime checkInAt = context.resolveCheckInOrNow();
        if (!checkInAt.isBefore(arriveAt.minusMinutes(30))) {
            return;
        }

        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("tableId is required for early-arrival check");
        }

        LocalDateTime recomputedCheckOut = checkInAt.plusMinutes(context.resolveSessionDurationMinutes());

        Integer excludeBookingId = context.resolveBookingId();
        List<TableBooking> nextBookings = tableBookingRepository.findConfirmedBookingsFromTime(
                tableId,
                checkInAt,
                BookingStatusEnum.CONFIRMED.getCode(),
                excludeBookingId
        );

        if (nextBookings == null || nextBookings.isEmpty()) {
            return;
        }

        LocalDateTime nextExpectedArrive = nextBookings.getFirst().getExpectedArriveTime();
        boolean hasConflict = nextExpectedArrive != null && !recomputedCheckOut.isBefore(nextExpectedArrive);

        if (hasConflict && !context.isForce()) {
            fail("early check-in conflicts with next confirmed booking; force=true is required");
        }

        if (hasConflict) {
            warn(context, "early check-in accepted with force; next booking may be impacted");
        }
    }
}
