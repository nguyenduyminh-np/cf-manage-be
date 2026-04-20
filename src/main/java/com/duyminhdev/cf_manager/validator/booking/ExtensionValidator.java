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
public class ExtensionValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_13_EXTENSION";
    }

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
            fail("Mã bàn (tableId) là bắt buộc để kiểm tra gia hạn");
        }

        Instant candidateCheckOut = context.resolveExpectedCheckOut();
        if (candidateCheckOut == null) {
            fail("Thời gian trả bàn mới (expectedCheckOut) là bắt buộc để kiểm tra gia hạn");
        }

        Instant now = context.getNow();
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

        Instant nextExpectedArrive = nextBookings.getFirst().getExpectedArriveTime();
        if (nextExpectedArrive != null && !nextExpectedArrive.isAfter(candidateCheckOut.plus(Duration.ofMinutes(30)))) {
            fail("Thời gian gia hạn xung đột với đặt bàn kế tiếp trong khoảng bảo vệ 30 phút");
        }
    }
}
