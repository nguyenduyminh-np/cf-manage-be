package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingSchedulerService {

    private final TableBookingRepository tableBookingRepository;
    private final TableRepository tableRepository;
    private final BookingUseCaseService bookingUseCaseService;
    private final BookingLockService bookingLockService;
    private final BookingNotificationService bookingNotificationService;

    @Transactional
    public void reserveTablesForUpcomingConfirmedBookings() {
        Instant now = Instant.now();
        Instant reserveWindowEnd = now.plus(Duration.ofMinutes(BookingSchedulerConstant.RESERVE_WINDOW_MINUTES));

        List<TableBooking> upcoming = tableBookingRepository.findConfirmedBookingsComingInWindow(
                now,
                reserveWindowEnd
        );

        for (TableBooking booking : upcoming) {
            Integer bookingId = booking.getId();
            Integer tableId = resolveTableId(booking);
            if (bookingId == null || tableId == null) {
                continue;
            }

            bookingLockService.runWithTableLock(tableId, () -> {
                tableBookingRepository.findByIdAndActiveTrue(bookingId).ifPresent(current -> {
                    if (!BookingStatusEnum.fromCode(current.getBookingStatus()).isConfirmed()) {
                        return;
                    }

                    Instant expectedArrive = current.getExpectedArriveTime();
                    if (expectedArrive == null || !expectedArrive.isAfter(now) || expectedArrive.isAfter(reserveWindowEnd)) {
                        return;
                    }

                    TableEntity table = current.getTable();
                    if (table == null || table.getId() == null) {
                        return;
                    }

                    TableEntity latestTable = tableRepository.findByIdAndActiveTrue(table.getId()).orElse(null);
                    if (latestTable == null) {
                        return;
                    }

                    if (!TableStatusEnum.fromCode(latestTable.getTableStatus()).isAvailable()) {
                        return;
                    }

                    latestTable.setTableStatus(TableStatusEnum.BOOKED.getCode());
                    tableRepository.save(latestTable);

                    bookingNotificationService.sendOnce(
                            BookingSchedulerConstant.TOPIC_TABLE_STATUS,
                            BookingSchedulerConstant.DEDUP_KEY_RESERVED_PREFIX + bookingId,
                            buildPayload(
                                    "TABLE_RESERVED",
                                    current,
                                    "Table is reserved 30 minutes before expected arrival"
                            )
                    );
                });
            });
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void expireNoShowBookings() {
        Instant now = Instant.now();
        List<TableBooking> noShowCandidates = tableBookingRepository.findNoShowCandidatesForExpiration(now);

        for (TableBooking candidate : noShowCandidates) {
            Integer bookingId = candidate.getId();
            if (bookingId == null) {
                continue;
            }

            try {
                TableBooking expired = bookingUseCaseService.expireBooking(bookingId);

                if (Boolean.TRUE.equals(expired.getDepositPaid())
                        && !Boolean.TRUE.equals(expired.getDepositForfeited())) {
                    expired.setDepositForfeited(true);
                    tableBookingRepository.save(expired);
                }

                bookingNotificationService.sendOnce(
                        BookingSchedulerConstant.TOPIC_BOOKING_UPDATES,
                        BookingSchedulerConstant.DEDUP_KEY_NO_SHOW_PREFIX + bookingId,
                        buildPayload("BOOKING_EXPIRED_NO_SHOW", expired, "Booking expired after no-show window")
                );
            } catch (Exception ex) {
                log.debug("Skip no-show expiration for bookingId={} because state changed: {}", bookingId, ex.getMessage());
            }
        }
    }

    public void notifyOccupiedTableConflicts() {
        Instant now = Instant.now();
        Instant reserveWindowEnd = now.plus(Duration.ofMinutes(BookingSchedulerConstant.RESERVE_WINDOW_MINUTES));

        List<TableBooking> upcoming = tableBookingRepository.findConfirmedBookingsComingInWindow(
                now,
                reserveWindowEnd
        );

        for (TableBooking booking : upcoming) {
            Integer bookingId = booking.getId();
            TableEntity table = booking.getTable();
            if (bookingId == null || table == null || table.getId() == null) {
                continue;
            }

            TableStatusEnum tableStatus = TableStatusEnum.fromCode(table.getTableStatus());
            if (!tableStatus.isOccupied()) {
                continue;
            }

            long minutesToArrive = Duration.between(now, booking.getExpectedArriveTime()).toMinutes();
            bookingNotificationService.sendOnce(
                    BookingSchedulerConstant.TOPIC_TABLE_ALERTS,
                    BookingSchedulerConstant.DEDUP_KEY_OCCUPIED_CONFLICT_PREFIX + bookingId,
                    buildPayload(
                            "TABLE_OCCUPIED_CONFLICT",
                            booking,
                            "Occupied table has confirmed booking in " + Math.max(minutesToArrive, 0L) + " minutes"
                    )
            );
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processNoOrderTimeoutFlow() {
        Instant now = Instant.now();
        List<TableBooking> checkedInCandidates = tableBookingRepository.findCheckedInWithoutOrderOlderThan(
            now,
            (int) BookingSchedulerConstant.NO_ORDER_WARNING_MINUTES
        );

        for (TableBooking booking : checkedInCandidates) {
            Integer bookingId = booking.getId();
            Integer tableId = resolveTableId(booking);
            Instant checkInAt = booking.getCheckInAt();
            if (bookingId == null || tableId == null || checkInAt == null) {
                continue;
            }

            long minutesSinceCheckIn = Duration.between(checkInAt, now).toMinutes();

            if (minutesSinceCheckIn >= BookingSchedulerConstant.NO_ORDER_CANCEL_MINUTES) {
                try {
                    TableBooking cancelled = bookingUseCaseService.cancelBookingNoOrderTimeout(bookingId, now);
                    bookingNotificationService.sendOnce(
                            BookingSchedulerConstant.TOPIC_TABLE_STATUS,
                            BookingSchedulerConstant.DEDUP_KEY_NO_ORDER_CANCEL_PREFIX + bookingId,
                            buildPayload(
                                    "NO_ORDER_AUTO_CANCELLED",
                                    cancelled,
                                    "Booking auto-cancelled after 20 minutes without orders"
                            )
                    );
                } catch (Exception ex) {
                    log.debug("Skip no-order cancellation for bookingId={} because state changed: {}", bookingId, ex.getMessage());
                }
                continue;
            }

            if (minutesSinceCheckIn >= BookingSchedulerConstant.NO_ORDER_WARNING_MINUTES) {
                bookingNotificationService.sendOnce(
                        BookingSchedulerConstant.TOPIC_TABLE_ALERTS,
                        BookingSchedulerConstant.DEDUP_KEY_NO_ORDER_WARN_PREFIX + bookingId,
                        buildPayload(
                                "NO_ORDER_WARNING",
                                booking,
                                "Booking has no order for 10 minutes"
                        )
                );
            }
        }
    }

    public void notifyBeforeExpectedCheckout() {
        Instant now = Instant.now();
        Instant reminderWindowEnd = now.plus(Duration.ofMinutes(BookingSchedulerConstant.CHECKOUT_REMINDER_MINUTES));

        List<TableBooking> aboutToCheckout = tableBookingRepository.findByStatusAndExpectedCheckOutWindow(
                BookingStatusEnum.CHECKED_IN.getCode(),
                now,
                reminderWindowEnd
        );

        for (TableBooking booking : aboutToCheckout) {
            Integer bookingId = booking.getId();
            if (bookingId == null) {
                continue;
            }

            long minutesToCheckout = Duration.between(now, booking.getExpectedCheckOut()).toMinutes();
            bookingNotificationService.sendOnce(
                    BookingSchedulerConstant.TOPIC_TABLE_ALERTS,
                    BookingSchedulerConstant.DEDUP_KEY_CHECKOUT_REMINDER_PREFIX + bookingId,
                    buildPayload(
                            "CHECKOUT_REMINDER",
                            booking,
                            "Booking is expected to checkout in " + Math.max(minutesToCheckout, 0L) + " minutes"
                    )
            );
        }
    }

    private Integer resolveTableId(TableBooking booking) {
        if (booking == null || booking.getTable() == null) {
            return null;
        }
        return booking.getTable().getId();
    }

    private Map<String, Object> buildPayload(String eventType, TableBooking booking, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", eventType);
        payload.put("message", message);
        payload.put("source", "SCHEDULER");

        if (booking != null) {
            payload.put("bookingId", booking.getId());
            payload.put("bookingStatus", booking.getBookingStatus());
            payload.put("expectedArriveTime", booking.getExpectedArriveTime());
            payload.put("expectedCheckOut", booking.getExpectedCheckOut());
            payload.put("tableId", resolveTableId(booking));
            payload.put("tableCode", booking.getTable() != null ? booking.getTable().getTableCode() : null);
        }

        payload.put("at", Instant.now());
        return payload;
    }
}
