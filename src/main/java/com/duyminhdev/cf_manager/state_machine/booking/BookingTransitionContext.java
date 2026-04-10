package com.duyminhdev.cf_manager.state_machine.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingTransitionContext {

    private final TableBooking booking;
    private final BookingStatusEnum targetStatus;
    private final LocalDateTime requestedCheckInAt;
    private final LocalDateTime requestedCheckOutAt;

    @Builder.Default
    private final boolean force = false;

    @Builder.Default
    private final boolean allowNoopTransition = true;
}
