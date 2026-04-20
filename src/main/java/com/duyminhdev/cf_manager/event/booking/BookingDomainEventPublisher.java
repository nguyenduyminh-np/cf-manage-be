package com.duyminhdev.cf_manager.event.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BookingDomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(BookingMutationType mutationType, Integer bookingId, Integer tableId) {
        BookingMutationEvent event = BookingMutationEvent.builder()
                .mutationType(mutationType)
                .bookingId(bookingId)
                .tableId(tableId)
            .occurredAt(Instant.now())
                .build();

        applicationEventPublisher.publishEvent(event);
    }
}
