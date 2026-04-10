package com.duyminhdev.cf_manager.event.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.service.booking.BookingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMutationAfterCommitListener {

    private final BookingNotificationService bookingNotificationService;
    private final TableBookingRepository tableBookingRepository;
    private final TableRepository tableRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingMutationAfterCommit(BookingMutationEvent event) {
        TableBooking booking = resolveBooking(event.getBookingId());
        Integer tableId = resolveTableId(event, booking);
        TableEntity table = tableId != null ? tableRepository.findByIdAndActiveTrue(tableId).orElse(null) : null;

        String bookingEventName = resolveBookingEventName(event.getMutationType());
        Map<String, Object> basePayload = buildPayload(event, booking, table, bookingEventName);

        bookingNotificationService.sendOnce(
                BookingSchedulerConstant.TOPIC_BOOKING_UPDATES,
                buildDedupKey(BookingSchedulerConstant.DEDUP_KEY_MUTATION_BOOKING_PREFIX, event),
                basePayload
        );

        if (affectsTableStatus(event.getMutationType()) && tableId != null) {
            Map<String, Object> tablePayload = new LinkedHashMap<>(basePayload);
            tablePayload.put("event", "TABLE_STATUS_SYNC");
            tablePayload.put("tableStatus", table != null ? table.getTableStatus() : null);
            bookingNotificationService.sendOnce(
                    BookingSchedulerConstant.TOPIC_TABLE_STATUS,
                    buildDedupKey(BookingSchedulerConstant.DEDUP_KEY_MUTATION_TABLE_PREFIX, event),
                    tablePayload
            );
        }

        if (event.getMutationType() == BookingMutationType.DEPOSIT) {
            Map<String, Object> depositPayload = new LinkedHashMap<>(basePayload);
            depositPayload.put("event", "BOOKING_DEPOSIT_PAID");
            bookingNotificationService.sendOnce(
                    BookingSchedulerConstant.TOPIC_DEPOSIT_EVENTS,
                    buildDedupKey(BookingSchedulerConstant.DEDUP_KEY_MUTATION_DEPOSIT_PREFIX, event),
                    depositPayload
            );
        }

        log.info(
                "Booking mutation committed: type={}, bookingId={}, tableId={}, at={}",
                event.getMutationType(),
                event.getBookingId(),
                event.getTableId(),
                event.getOccurredAt()
        );
    }

    private TableBooking resolveBooking(Integer bookingId) {
        if (bookingId == null) {
            return null;
        }
        return tableBookingRepository.findByIdAndActiveTrue(bookingId).orElse(null);
    }

    private Integer resolveTableId(BookingMutationEvent event, TableBooking booking) {
        if (event.getTableId() != null) {
            return event.getTableId();
        }
        if (booking == null || booking.getTable() == null) {
            return null;
        }
        return booking.getTable().getId();
    }

    private String resolveBookingEventName(BookingMutationType mutationType) {
        return switch (mutationType) {
            case CREATE -> "BOOKING_CREATED";
            case UPDATE -> "BOOKING_UPDATED";
            case UPDATE_STATUS -> "BOOKING_STATUS_UPDATED";
            case CONFIRM -> "BOOKING_CONFIRMED";
            case CHECK_IN -> "BOOKING_CHECKED_IN";
            case CHECK_OUT -> "BOOKING_CHECKED_OUT";
            case CANCEL -> "BOOKING_CANCELLED";
            case EXPIRE -> "BOOKING_EXPIRED";
            case EXTEND -> "BOOKING_EXTENDED";
            case WALK_IN -> "BOOKING_WALK_IN_CREATED";
            case LATE_ARRIVAL_WALK_IN -> "BOOKING_LATE_ARRIVAL_WALK_IN_CREATED";
            case CANCEL_NO_ORDER_TIMEOUT -> "BOOKING_AUTO_CANCELLED_NO_ORDER";
            case DEPOSIT -> "BOOKING_DEPOSIT_UPDATED";
        };
    }

    private boolean affectsTableStatus(BookingMutationType mutationType) {
        return mutationType != BookingMutationType.DEPOSIT;
    }

    private String buildDedupKey(String prefix, BookingMutationEvent event) {
        return prefix
                + event.getMutationType()
                + ":"
                + event.getBookingId()
                + ":"
                + event.getTableId()
                + ":"
                + event.getOccurredAt();
    }

    private Map<String, Object> buildPayload(
            BookingMutationEvent event,
            TableBooking booking,
            TableEntity table,
            String eventName
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", eventName);
        payload.put("mutationType", event.getMutationType().name());
        payload.put("bookingId", event.getBookingId());
        payload.put("tableId", resolveTableId(event, booking));
        payload.put("tableCode", table != null ? table.getTableCode() : null);
        payload.put("tableStatus", table != null ? table.getTableStatus() : null);
        payload.put("bookingStatus", booking != null ? booking.getBookingStatus() : null);
        payload.put("at", event.getOccurredAt());
        payload.put("message", "Booking mutation committed: " + eventName);
        payload.put("source", "BOOKING_MUTATION_AFTER_COMMIT");
        return payload;
    }
}
