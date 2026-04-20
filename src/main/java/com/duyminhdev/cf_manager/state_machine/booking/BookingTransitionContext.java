package com.duyminhdev.cf_manager.state_machine.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BookingTransitionContext {

    private final TableBooking booking;
    private final BookingStatusEnum targetStatus;
    private final Instant requestedCheckInAt;
    private final Instant requestedCheckOutAt;

    @Builder.Default
    private final boolean force = false;

    @Builder.Default
    private final boolean allowNoopTransition = true;
}
