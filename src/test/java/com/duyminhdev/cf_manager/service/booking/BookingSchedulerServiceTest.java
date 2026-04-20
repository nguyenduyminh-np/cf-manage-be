package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingSchedulerServiceTest {

    @Mock
    private TableBookingRepository tableBookingRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private BookingUseCaseService bookingUseCaseService;

    @Mock
    private BookingLockService bookingLockService;

    @Mock
    private BookingNotificationService bookingNotificationService;

    @InjectMocks
    private BookingSchedulerService bookingSchedulerService;

    @Test
    void shouldReserveAvailableTableForUpcomingConfirmedBooking() {
                doAnswer(invocation -> {
                        Runnable runnable = invocation.getArgument(1);
                        runnable.run();
                        return null;
                }).when(bookingLockService).runWithTableLock(anyInt(), any(Runnable.class));

        TableBooking booking = booking(10, 3, BookingStatusEnum.CONFIRMED, TableStatusEnum.AVAILABLE,
                Instant.now().plus(Duration.ofMinutes(20)), Instant.now().plus(Duration.ofHours(2)), Instant.now().minus(Duration.ofMinutes(1)));

        when(tableBookingRepository.findConfirmedBookingsComingInWindow(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(booking));
        when(tableBookingRepository.findByIdAndActiveTrue(10)).thenReturn(Optional.of(booking));
        when(tableRepository.findByIdAndActiveTrue(3)).thenReturn(Optional.of(booking.getTable()));

        bookingSchedulerService.reserveTablesForUpcomingConfirmedBookings();

        ArgumentCaptor<TableEntity> tableCaptor = ArgumentCaptor.forClass(TableEntity.class);
        verify(tableRepository).save(tableCaptor.capture());
        assertEquals(TableStatusEnum.BOOKED.getCode(), tableCaptor.getValue().getTableStatus());

        verify(bookingNotificationService).sendOnce(
                anyString(),
                anyString(),
                any()
        );
    }

    @Test
    void shouldExpireNoShowBookingUsingUseCaseService() {
        TableBooking candidate = booking(19, 5, BookingStatusEnum.CONFIRMED, TableStatusEnum.BOOKED,
                Instant.now().minus(Duration.ofMinutes(50)), Instant.now().plus(Duration.ofMinutes(20)), null);
        TableBooking expired = booking(19, 5, BookingStatusEnum.EXPIRED, TableStatusEnum.AVAILABLE,
                candidate.getExpectedArriveTime(), candidate.getExpectedCheckOut(), null);

        when(tableBookingRepository.findNoShowCandidatesForExpiration(any(Instant.class)))
                .thenReturn(List.of(candidate));
        when(bookingUseCaseService.expireBooking(19)).thenReturn(expired);

        bookingSchedulerService.expireNoShowBookings();

        verify(bookingUseCaseService).expireBooking(19);
        verify(bookingNotificationService).sendOnce(
                anyString(),
                anyString(),
                any()
        );
    }

    @Test
    void shouldWarnAfterTenMinutesWithoutOrder() {
        TableBooking checkedIn = booking(41, 7, BookingStatusEnum.CHECKED_IN, TableStatusEnum.OCCUPIED,
                Instant.now().minus(Duration.ofMinutes(40)), Instant.now().plus(Duration.ofMinutes(30)), Instant.now().minus(Duration.ofMinutes(11)));

        when(tableBookingRepository.findCheckedInWithoutOrderOlderThan(any(Instant.class), anyInt()))
                .thenReturn(List.of(checkedIn));

        bookingSchedulerService.processNoOrderTimeoutFlow();

        verify(bookingNotificationService).sendOnce(
                anyString(),
                anyString(),
                any()
        );
        verify(bookingUseCaseService, never()).cancelBookingNoOrderTimeout(anyInt(), any(Instant.class));
    }

    @Test
    void shouldAutoCancelAfterTwentyMinutesWithoutOrder() {
        TableBooking checkedIn = booking(42, 8, BookingStatusEnum.CHECKED_IN, TableStatusEnum.OCCUPIED,
                Instant.now().minus(Duration.ofMinutes(60)), Instant.now().plus(Duration.ofMinutes(20)), Instant.now().minus(Duration.ofMinutes(25)));
        TableBooking cancelled = booking(42, 8, BookingStatusEnum.CANCELLED, TableStatusEnum.AVAILABLE,
                checkedIn.getExpectedArriveTime(), checkedIn.getExpectedCheckOut(), checkedIn.getCheckInAt());

        when(tableBookingRepository.findCheckedInWithoutOrderOlderThan(any(Instant.class), anyInt()))
                .thenReturn(List.of(checkedIn));
        when(bookingUseCaseService.cancelBookingNoOrderTimeout(anyInt(), any(Instant.class))).thenReturn(cancelled);

        bookingSchedulerService.processNoOrderTimeoutFlow();

        verify(bookingUseCaseService).cancelBookingNoOrderTimeout(anyInt(), any(Instant.class));
        verify(bookingNotificationService).sendOnce(
                anyString(),
                anyString(),
                any()
        );
    }

    private TableBooking booking(
            Integer bookingId,
            Integer tableId,
            BookingStatusEnum status,
            TableStatusEnum tableStatus,
            Instant expectedArrive,
            Instant expectedCheckout,
            Instant checkInAt
    ) {
        TableEntity table = TableEntity.builder()
                .id(tableId)
                .tableCode("T-" + tableId)
                .tableStatus(tableStatus.getCode())
                .active(true)
                .build();

        return TableBooking.builder()
                .id(bookingId)
                .table(table)
                .active(true)
                .bookingStatus(status.getCode())
                .expectedArriveTime(expectedArrive)
                .expectedCheckOut(expectedCheckout)
                .checkInAt(checkInAt)
                .build();
    }

    private static Instant at(int year, int month, int day, int hour, int minute) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, 0).toInstant(java.time.ZoneOffset.UTC);
    }
}
