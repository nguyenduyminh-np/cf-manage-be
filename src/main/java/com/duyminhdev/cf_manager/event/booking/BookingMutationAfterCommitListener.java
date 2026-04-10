package com.duyminhdev.cf_manager.event.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class BookingMutationAfterCommitListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingMutationAfterCommit(BookingMutationEvent event) {
        // Module 9 will wire this hook to websocket notifications.
        log.info(
                "Booking mutation committed: type={}, bookingId={}, tableId={}, at={}",
                event.getMutationType(),
                event.getBookingId(),
                event.getTableId(),
                event.getOccurredAt()
        );
    }
}
