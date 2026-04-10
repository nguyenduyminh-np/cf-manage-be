package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class BookingValidationContext {

    private final TableBooking booking;
    private final TableEntity table;
    private final BookingStatusEnum currentStatus;
    private final BookingStatusEnum targetStatus;
    private final LocalDateTime requestedCheckInAt;
    private final LocalDateTime requestedCheckOutAt;
    private final LocalDateTime expectedArriveTime;
    private final LocalDateTime expectedCheckOut;

    @Builder.Default
    private final LocalDateTime now = LocalDateTime.now();

    @Builder.Default
    private final boolean force = false;

    private final Integer excludeBookingId;

    @Builder.Default
    private final List<String> warnings = new ArrayList<>();

    public Integer resolveTableId() {
        if (table != null && table.getId() != null) {
            return table.getId();
        }

        if (booking != null && booking.getTable() != null) {
            return booking.getTable().getId();
        }

        return null;
    }

    public Integer resolveBookingId() {
        if (excludeBookingId != null) {
            return excludeBookingId;
        }
        return booking != null ? booking.getId() : null;
    }

    public LocalDateTime resolveExpectedArriveTime() {
        if (expectedArriveTime != null) {
            return expectedArriveTime;
        }
        return booking != null ? booking.getExpectedArriveTime() : null;
    }

    public LocalDateTime resolveExpectedCheckOut() {
        if (expectedCheckOut != null) {
            return expectedCheckOut;
        }
        return booking != null ? booking.getExpectedCheckOut() : null;
    }

    public LocalDateTime resolveCheckInOrNow() {
        if (requestedCheckInAt != null) {
            return requestedCheckInAt;
        }

        if (booking != null && booking.getCheckInAt() != null) {
            return booking.getCheckInAt();
        }

        return now;
    }

    public long resolveSessionDurationMinutes() {
        LocalDateTime arrive = resolveExpectedArriveTime();
        LocalDateTime checkOut = resolveExpectedCheckOut();

        if (arrive == null || checkOut == null) {
            return 120L;
        }

        long minutes = java.time.Duration.between(arrive, checkOut).toMinutes();
        return minutes > 0 ? minutes : 120L;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }
}
