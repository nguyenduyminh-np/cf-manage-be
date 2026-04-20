package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingOwnershipValidator extends AbstractBookingValidationRule {

    private final TableBookingRepository tableBookingRepository;

    @Override
    protected String ruleTag() {
        return "RULE_11_BOOKING_OWNERSHIP";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        return useCase == BookingValidationUseCase.CHECK_IN;
    }

    @Override
    public void validate(BookingValidationContext context) {
        TableBooking booking = context.getBooking();
        if (booking == null) {
            fail("Thông tin đặt bàn (booking) là bắt buộc để kiểm tra quyền sử dụng bàn");
        }

        if (booking.getTable() == null || booking.getTable().getTableStatus() == null) {
            return;
        }

        Instant checkInAt = context.resolveCheckInOrNow();
        Instant arriveAt = context.resolveExpectedArriveTime();
        if (arriveAt == null || !checkInAt.isBefore(arriveAt)) {
            return;
        }

        String tableStatusCode = booking.getTable().getTableStatus();
        if (!TableStatusEnum.BOOKED.getCode().equalsIgnoreCase(tableStatusCode)) {
            return;
        }

        Integer tableId = context.resolveTableId();
        List<TableBooking> nearestConfirmed = tableBookingRepository.findConfirmedBookingsFromTime(
                tableId,
                context.getNow().minus(Duration.ofMinutes(30)),
                BookingStatusEnum.CONFIRMED.getCode(),
                null
        );

        if (nearestConfirmed == null || nearestConfirmed.isEmpty()) {
            return;
        }

        Integer ownerBookingId = nearestConfirmed.getFirst().getId();
        if (ownerBookingId != null && !ownerBookingId.equals(booking.getId())) {
            fail("Bàn đã được giữ cho đặt bàn khác; không thể thực hiện check-in lúc này");
        }
    }
}
