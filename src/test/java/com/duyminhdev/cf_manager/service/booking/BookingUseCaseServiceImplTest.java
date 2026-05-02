package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.event.booking.BookingDomainEventPublisher;
import com.duyminhdev.cf_manager.event.booking.BookingMutationType;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.mapper.TableBookingMapper;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.state_machine.booking.BookingStateMachine;
import com.duyminhdev.cf_manager.state_machine.booking.BookingTransitionContext;
import com.duyminhdev.cf_manager.utils.InvoiceCodeService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import com.duyminhdev.cf_manager.validator.booking.BookingRuleValidatorChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingUseCaseServiceImplTest {

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
    private BookingRuleValidatorChain bookingRuleValidatorChain;

    @Mock
    private BookingDomainEventPublisher bookingDomainEventPublisher;

    @Mock
    private InvoiceCodeService invoiceCodeService;

    private BookingUseCaseServiceImpl bookingUseCaseService;

    @BeforeEach
    void setUp() {
        bookingUseCaseService = new BookingUseCaseServiceImpl(
                tableBookingRepository,
                tableBookingMapper,
                serviceSupport,
                bookingStateMachine,
                bookingLockService,
                bookingRuleValidatorChain,
                bookingDomainEventPublisher,
                invoiceCodeService
        );
    }

    @Test
    void markDepositPaid_shouldAutoConfirmWhenStatusIsPending() {
        TableBooking booking = basePendingBooking();
        Integer bookingId = booking.getId();
        Integer tableId = booking.getTable().getId();

        when(tableBookingRepository.findByIdAndActiveTrue(bookingId))
                .thenReturn(Optional.of(booking), Optional.of(booking));
        when(bookingLockService.executeWithTableLock(eq(tableId), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<TableBooking> action = invocation.getArgument(1);
                    return action.get();
                });
        when(tableBookingRepository.save(booking)).thenReturn(booking);

        Instant paidAt = at(2026, 4, 15, 11, 30);
        bookingUseCaseService.markDepositPaid(bookingId, BigDecimal.valueOf(100_000), "TXN-100", paidAt);

        ArgumentCaptor<BookingTransitionContext> transitionCaptor = ArgumentCaptor.forClass(BookingTransitionContext.class);
        verify(bookingStateMachine).transition(transitionCaptor.capture());
        assertEquals(BookingStatusEnum.CONFIRMED, transitionCaptor.getValue().getTargetStatus());
        assertTrue(booking.getDepositPaid());
        assertEquals(BigDecimal.valueOf(100_000), booking.getDepositAmount());

        verify(serviceSupport).recomputeAndSyncTableStatus(tableId);
        verify(bookingDomainEventPublisher).publish(BookingMutationType.DEPOSIT, bookingId, tableId);
    }

    private TableBooking basePendingBooking() {
        TableEntity table = TableEntity.builder()
                .id(5)
                .tableStatus(TableStatusEnum.AVAILABLE.getCode())
                .active(true)
                .build();

        return TableBooking.builder()
                .id(12)
                .table(table)
                .active(true)
                .bookingStatus(BookingStatusEnum.PENDING.getCode())
                .expectedArriveTime(at(2026, 4, 15, 19, 0))
                .expectedCheckOut(at(2026, 4, 15, 21, 0))
                .build();
    }

    private static Instant at(int year, int month, int day, int hour, int minute) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, 0).toInstant(java.time.ZoneOffset.UTC);
    }
}
