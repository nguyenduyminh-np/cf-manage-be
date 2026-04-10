package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
    private DishOrderRepository dishOrderRepository;

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
                LocalDateTime.now().plusMinutes(20), LocalDateTime.now().plusHours(2), LocalDateTime.now().minusMinutes(1));

        when(tableBookingRepository.findBookingsByStatusAndExpectedArriveWindow(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
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
                LocalDateTime.now().minusMinutes(50), LocalDateTime.now().plusMinutes(20), null);
        TableBooking expired = booking(19, 5, BookingStatusEnum.EXPIRED, TableStatusEnum.AVAILABLE,
                candidate.getExpectedArriveTime(), candidate.getExpectedCheckOut(), null);

        when(tableBookingRepository.findNoShowCandidates(anyString(), any(LocalDateTime.class)))
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
                LocalDateTime.now().minusMinutes(40), LocalDateTime.now().plusMinutes(30), LocalDateTime.now().minusMinutes(11));

        when(tableBookingRepository.findCheckedInByCheckInBefore(anyString(), any(LocalDateTime.class)))
                .thenReturn(List.of(checkedIn));
        when(dishOrderRepository.existsActiveOrderOnTableFromTime(7, checkedIn.getCheckInAt())).thenReturn(false);

        bookingSchedulerService.processNoOrderTimeoutFlow();

        verify(bookingNotificationService).sendOnce(
                anyString(),
                anyString(),
                any()
        );
        verify(bookingUseCaseService, never()).cancelBookingNoOrderTimeout(anyInt(), any(LocalDateTime.class));
    }

    @Test
    void shouldAutoCancelAfterTwentyMinutesWithoutOrder() {
        TableBooking checkedIn = booking(42, 8, BookingStatusEnum.CHECKED_IN, TableStatusEnum.OCCUPIED,
                LocalDateTime.now().minusMinutes(60), LocalDateTime.now().plusMinutes(20), LocalDateTime.now().minusMinutes(25));
        TableBooking cancelled = booking(42, 8, BookingStatusEnum.CANCELLED, TableStatusEnum.AVAILABLE,
                checkedIn.getExpectedArriveTime(), checkedIn.getExpectedCheckOut(), checkedIn.getCheckInAt());

        when(tableBookingRepository.findCheckedInByCheckInBefore(anyString(), any(LocalDateTime.class)))
                .thenReturn(List.of(checkedIn));
        when(dishOrderRepository.existsActiveOrderOnTableFromTime(8, checkedIn.getCheckInAt())).thenReturn(false);
        when(bookingUseCaseService.cancelBookingNoOrderTimeout(anyInt(), any(LocalDateTime.class))).thenReturn(cancelled);

        bookingSchedulerService.processNoOrderTimeoutFlow();

        verify(bookingUseCaseService).cancelBookingNoOrderTimeout(anyInt(), any(LocalDateTime.class));
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
            LocalDateTime expectedArrive,
            LocalDateTime expectedCheckout,
            LocalDateTime checkInAt
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
}
