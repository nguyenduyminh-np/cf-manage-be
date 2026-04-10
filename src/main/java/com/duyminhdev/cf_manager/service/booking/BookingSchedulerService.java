package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final DishOrderRepository dishOrderRepository;
    private final BookingUseCaseService bookingUseCaseService;
    private final BookingLockService bookingLockService;
    private final BookingNotificationService bookingNotificationService;

    @Transactional
    public void reserveTablesForUpcomingConfirmedBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reserveWindowEnd = now.plusMinutes(BookingSchedulerConstant.RESERVE_WINDOW_MINUTES);

        List<TableBooking> upcoming = tableBookingRepository.findBookingsByStatusAndExpectedArriveWindow(
                BookingStatusEnum.CONFIRMED.getCode(),
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

                    LocalDateTime expectedArrive = current.getExpectedArriveTime();
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

    @Transactional
    public void expireNoShowBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(BookingSchedulerConstant.NO_SHOW_GRACE_MINUTES);

        List<TableBooking> noShowCandidates = tableBookingRepository.findNoShowCandidates(
                BookingStatusEnum.CONFIRMED.getCode(),
                cutoff
        );

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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reserveWindowEnd = now.plusMinutes(BookingSchedulerConstant.RESERVE_WINDOW_MINUTES);

        List<TableBooking> upcoming = tableBookingRepository.findBookingsByStatusAndExpectedArriveWindow(
                BookingStatusEnum.CONFIRMED.getCode(),
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

    @Transactional
    public void processNoOrderTimeoutFlow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningCutoff = now.minusMinutes(BookingSchedulerConstant.NO_ORDER_WARNING_MINUTES);

        List<TableBooking> checkedInCandidates = tableBookingRepository.findCheckedInByCheckInBefore(
                BookingStatusEnum.CHECKED_IN.getCode(),
                warningCutoff
        );

        for (TableBooking booking : checkedInCandidates) {
            Integer bookingId = booking.getId();
            Integer tableId = resolveTableId(booking);
            LocalDateTime checkInAt = booking.getCheckInAt();
            if (bookingId == null || tableId == null || checkInAt == null) {
                continue;
            }

            if (dishOrderRepository.existsActiveOrderOnTableFromTime(tableId, checkInAt)) {
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindowEnd = now.plusMinutes(BookingSchedulerConstant.CHECKOUT_REMINDER_MINUTES);

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
        payload.put("eventType", eventType);
        payload.put("message", message);

        if (booking != null) {
            payload.put("bookingId", booking.getId());
            payload.put("status", booking.getBookingStatus());
            payload.put("expectedArriveTime", booking.getExpectedArriveTime());
            payload.put("expectedCheckOut", booking.getExpectedCheckOut());
            payload.put("tableId", resolveTableId(booking));
            payload.put("tableCode", booking.getTable() != null ? booking.getTable().getTableCode() : null);
        }

        payload.put("occurredAt", LocalDateTime.now());
        return payload;
    }
}
