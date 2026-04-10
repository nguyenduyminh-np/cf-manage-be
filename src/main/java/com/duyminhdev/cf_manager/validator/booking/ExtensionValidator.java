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
public class ExtensionValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.EXTEND_BOOKING;
    }

    @Override
    public void validate(BookingValidationContext context) {
        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("tableId is required for extension check");
        }

        LocalDateTime candidateCheckOut = context.resolveExpectedCheckOut();
        if (candidateCheckOut == null) {
            fail("expectedCheckOut is required for extension check");
        }

        LocalDateTime now = context.getNow();
        Integer excludeBookingId = context.resolveBookingId();

        List<TableBooking> nextBookings = tableBookingRepository.findConfirmedBookingsFromTime(
                tableId,
                now,
                BookingStatusEnum.CONFIRMED.getCode(),
                excludeBookingId
        );

        if (nextBookings == null || nextBookings.isEmpty()) {
            return;
        }

        LocalDateTime nextExpectedArrive = nextBookings.getFirst().getExpectedArriveTime();
        if (nextExpectedArrive != null && !nextExpectedArrive.isAfter(candidateCheckOut.plusMinutes(30))) {
            fail("extension conflicts with next confirmed booking within protected 30-minute window");
        }
    }
}
