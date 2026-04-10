package com.duyminhdev.cf_manager.event.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.service.booking.BookingNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMutationAfterCommitListenerTest {

    @Mock
    private BookingNotificationService bookingNotificationService;

    @Mock
    private TableBookingRepository tableBookingRepository;

    @Mock
    private TableRepository tableRepository;

    @InjectMocks
    private BookingMutationAfterCommitListener listener;

    @Test
    void shouldPublishBookingAndTableTopicsForCheckInMutation() {
        TableEntity table = TableEntity.builder()
                .id(7)
                .tableCode("T07")
                .tableStatus("OCCUPIED")
                .active(true)
                .build();

        TableBooking booking = TableBooking.builder()
                .id(101)
                .table(table)
                .bookingStatus("CHECKED_IN")
                .active(true)
                .build();

        BookingMutationEvent event = BookingMutationEvent.builder()
                .mutationType(BookingMutationType.CHECK_IN)
                .bookingId(101)
                .tableId(7)
                .occurredAt(LocalDateTime.now())
                .build();

        when(tableBookingRepository.findByIdAndActiveTrue(101)).thenReturn(Optional.of(booking));
        when(tableRepository.findByIdAndActiveTrue(7)).thenReturn(Optional.of(table));

        listener.onBookingMutationAfterCommit(event);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(bookingNotificationService, atLeastOnce()).sendOnce(eq(BookingSchedulerConstant.TOPIC_BOOKING_UPDATES), any(), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("BOOKING_CHECKED_IN", payload.get("event"));
        assertEquals(101, payload.get("bookingId"));
        assertEquals(7, payload.get("tableId"));
        assertNotNull(payload.get("message"));

        verify(bookingNotificationService).sendOnce(eq(BookingSchedulerConstant.TOPIC_TABLE_STATUS), any(), any());
        verify(bookingNotificationService, never()).sendOnce(eq(BookingSchedulerConstant.TOPIC_DEPOSIT_EVENTS), any(), any());
    }

    @Test
    void shouldPublishDepositTopicForDepositMutation() {
        TableEntity table = TableEntity.builder()
                .id(5)
                .tableCode("T05")
                .tableStatus("AVAILABLE")
                .active(true)
                .build();

        TableBooking booking = TableBooking.builder()
                .id(202)
                .table(table)
                .bookingStatus("CONFIRMED")
                .active(true)
                .build();

        BookingMutationEvent event = BookingMutationEvent.builder()
                .mutationType(BookingMutationType.DEPOSIT)
                .bookingId(202)
                .tableId(5)
                .occurredAt(LocalDateTime.now())
                .build();

        when(tableBookingRepository.findByIdAndActiveTrue(202)).thenReturn(Optional.of(booking));
        when(tableRepository.findByIdAndActiveTrue(5)).thenReturn(Optional.of(table));

        listener.onBookingMutationAfterCommit(event);

        verify(bookingNotificationService).sendOnce(eq(BookingSchedulerConstant.TOPIC_BOOKING_UPDATES), any(), any());
        verify(bookingNotificationService).sendOnce(eq(BookingSchedulerConstant.TOPIC_DEPOSIT_EVENTS), any(), any());
        verify(bookingNotificationService, never()).sendOnce(eq(BookingSchedulerConstant.TOPIC_TABLE_STATUS), any(), any());
    }
}
