package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingAvailableSlotsRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingAvailableSlotResponseDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.event.booking.BookingDomainEventPublisher;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.mapper.TableBookingMapper;
import com.duyminhdev.cf_manager.repository.NativeSqlTableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.service.booking.BookingUseCaseService;
import com.duyminhdev.cf_manager.state_machine.booking.BookingStateMachine;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableBookingServiceImplTest {

    @Mock
    private TableBookingRepository tableBookingRepository;

    @Mock
    private TableBookingMapper tableBookingMapper;

    @Mock
    private ServiceSupport serviceSupport;

    @Mock
    private BookingStateMachine bookingStateMachine;

    @Mock
    private BookingLockService bookingLockService;

    @Mock
    private BookingDomainEventPublisher bookingDomainEventPublisher;

    @Mock
    private BookingUseCaseService bookingUseCaseService;

    @Mock
    private NativeSqlTableBookingRepository nativeSqlTableBookingRepository;

    private TableBookingServiceImpl tableBookingService;

    @BeforeEach
    void setUp() {
        tableBookingService = new TableBookingServiceImpl(
                tableBookingRepository,
                tableBookingMapper,
                serviceSupport,
                bookingStateMachine,
                bookingLockService,
                bookingDomainEventPublisher,
                bookingUseCaseService,
                nativeSqlTableBookingRepository
        );
    }

    @Test
    void getAvailableSlots_shouldReturnGapsBetweenBlockingBookings() {
        Integer tableId = 8;
        LocalDate date = LocalDate.of(2026, 4, 10);
        Instant dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant dayEnd = dayStart.plus(Duration.ofDays(1));

        when(serviceSupport.getActiveTable(tableId)).thenReturn(TableEntity.builder().id(tableId).active(true).build());
        when(tableBookingRepository.findActiveBookingsOnTableBetweenByStatuses(
                eq(tableId),
                eq(dayStart),
                eq(dayEnd),
                anyList()
        )).thenReturn(List.of(
                booking(at(2026, 4, 10, 10, 0), at(2026, 4, 10, 12, 0), BookingStatusEnum.CONFIRMED),
                booking(at(2026, 4, 10, 13, 30), at(2026, 4, 10, 15, 0), BookingStatusEnum.CHECKED_IN)
        ));

        TableBookingAvailableSlotsRequestDTO request = new TableBookingAvailableSlotsRequestDTO();
        request.setTableId(tableId);
        request.setDate(date);

        List<TableBookingAvailableSlotResponseDTO> slots = tableBookingService.getAvailableSlots(request);

        assertEquals(3, slots.size());
        assertEquals(dayStart, slots.get(0).getSlotStart());
        assertEquals(at(2026, 4, 10, 10, 0), slots.get(0).getSlotEnd());

        assertEquals(at(2026, 4, 10, 12, 0), slots.get(1).getSlotStart());
        assertEquals(at(2026, 4, 10, 13, 30), slots.get(1).getSlotEnd());

        assertEquals(at(2026, 4, 10, 15, 0), slots.get(2).getSlotStart());
        assertEquals(dayEnd, slots.get(2).getSlotEnd());
    }

    private Instant at(int year, int month, int day, int hour, int minute) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, 0).toInstant(java.time.ZoneOffset.UTC);
    }

    private TableBooking booking(Instant start, Instant end, BookingStatusEnum status) {
        return TableBooking.builder()
                .id((int) ((start.getEpochSecond() / 3600) * 100 + (end.getEpochSecond() / 3600)))
                .active(true)
                .bookingStatus(status.getCode())
                .expectedArriveTime(start)
                .expectedCheckOut(end)
                .build();
    }
}
