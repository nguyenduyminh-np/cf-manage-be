package com.duyminhdev.cf_manager.event.booking;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BookingMutationEvent {

    private final BookingMutationType mutationType;
    private final Integer bookingId;
    private final Integer tableId;
    private final Instant occurredAt;
}
