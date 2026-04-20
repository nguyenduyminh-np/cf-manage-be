package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EarlyArrivalValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_10_EARLY_ARRIVAL";
    }

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
        Instant arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null) {
            fail("Thời gian đến dự kiến (expectedArriveTime) không được để trống");
        }

        Instant checkInAt = context.resolveCheckInOrNow();
        if (!checkInAt.isBefore(arriveAt.minus(Duration.ofMinutes(30)))) {
            return;
        }

        Integer tableId = context.resolveTableId();
        if (tableId == null) {
            fail("Mã bàn (tableId) là bắt buộc để kiểm tra check-in sớm");
        }

        Instant recomputedCheckOut = checkInAt.plus(Duration.ofMinutes(context.resolveSessionDurationMinutes()));

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

        Instant nextExpectedArrive = nextBookings.getFirst().getExpectedArriveTime();
        boolean hasConflict = nextExpectedArrive != null && !recomputedCheckOut.isBefore(nextExpectedArrive);

        if (hasConflict && !context.isForce()) {
            fail("Check-in sớm gây xung đột với đặt bàn kế tiếp; yêu cầu gửi kèm force=true để buộc xử lý");
        }

        if (hasConflict) {
            warn(context, "Check-in sớm được chấp nhận với force; các đặt bàn tiếp theo có thể bị ảnh hưởng");
        }
    }
}
