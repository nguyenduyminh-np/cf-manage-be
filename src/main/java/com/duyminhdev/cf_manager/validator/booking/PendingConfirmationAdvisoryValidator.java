package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingConfirmationAdvisoryValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    public int order() {
        return 20;
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
            fail("tableId is required for pending-confirmation advisory");
        }

        boolean hasPending = tableBookingRepository.existsActiveBookingOnTableFromTimeAndStatuses(
                tableId,
                context.getNow(),
                java.util.List.of(BookingStatusEnum.PENDING.getCode()),
                context.resolveBookingId()
        );

        if (hasPending) {
            warn(context, "table has pending confirmation bookings; staff should contact customer before walk-in assignment");
        }
    }
}
