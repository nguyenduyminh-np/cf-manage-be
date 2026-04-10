package com.duyminhdev.cf_manager.state_machine.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.exceptions.BookingStateTransitionException;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import com.duyminhdev.cf_manager.validator.booking.BookingRuleValidatorChain;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BookingStateMachineImplTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private ServiceSupport serviceSupport;

    @Mock
    private BookingRuleValidatorChain bookingRuleValidatorChain;

    @InjectMocks
    private BookingStateMachineImpl bookingStateMachine;

    @BeforeEach
    void setUp() {
        TableEntity table = TableEntity.builder()
                .id(1)
                .active(true)
                .build();

        lenient().when(tableRepository.findByIdAndActiveTrueForUpdate(anyInt())).thenReturn(Optional.of(table));
        lenient().doNothing().when(serviceSupport).validateBookingTimes(any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowDefinedTransitions(BookingStatusEnum from, BookingStatusEnum to) {
        TableBooking booking = baseBooking(from);
        adaptBookingForTransition(booking, from, to);

        BookingTransitionContext context = BookingTransitionContext.builder()
                .booking(booking)
                .targetStatus(to)
                .allowNoopTransition(true)
                .build();

        TableBooking result = bookingStateMachine.transition(context);

        assertEquals(to.getCode(), result.getBookingStatus());
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransitions(BookingStatusEnum from, BookingStatusEnum to) {
        TableBooking booking = baseBooking(from);

        BookingTransitionContext context = BookingTransitionContext.builder()
                .booking(booking)
                .targetStatus(to)
                .allowNoopTransition(true)
                .build();

        BookingStateTransitionException ex = assertThrows(
                BookingStateTransitionException.class,
                () -> bookingStateMachine.transition(context)
        );

        assertTrue(ex.getMessage().startsWith("BOOKING_TRANSITION_NOT_ALLOWED"));
    }

    @Test
    void shouldRejectConfirmWhenDepositIsUnpaid() {
        TableBooking booking = baseBooking(BookingStatusEnum.PENDING);
        booking.setDepositAmount(BigDecimal.valueOf(100_000));
        booking.setDepositPaid(false);

        doThrow(new BookingStateTransitionException("BOOKING_TRANSITION_RULE_VIOLATION: cannot confirm booking when deposit is unpaid"))
            .when(bookingRuleValidatorChain)
            .validate(eq(BookingValidationUseCase.CONFIRM_BOOKING), any());

        BookingTransitionContext context = BookingTransitionContext.builder()
                .booking(booking)
                .targetStatus(BookingStatusEnum.CONFIRMED)
                .allowNoopTransition(true)
                .build();

        BookingStateTransitionException ex = assertThrows(
                BookingStateTransitionException.class,
                () -> bookingStateMachine.transition(context)
        );

        assertTrue(ex.getMessage().startsWith("BOOKING_TRANSITION_RULE_VIOLATION"));
    }

    @Test
    void shouldRejectInactiveBookingInGuard() {
        TableBooking booking = baseBooking(BookingStatusEnum.PENDING);
        booking.setActive(false);

        BookingTransitionContext context = BookingTransitionContext.builder()
                .booking(booking)
                .targetStatus(BookingStatusEnum.CONFIRMED)
                .allowNoopTransition(true)
                .build();

        BookingStateTransitionException ex = assertThrows(
                BookingStateTransitionException.class,
                () -> bookingStateMachine.transition(context)
        );

        assertTrue(ex.getMessage().startsWith("BOOKING_TRANSITION_GUARD_FAILED"));
    }

    @Test
    void shouldAutoFillCheckInAndCheckOutWhenTransitionToCompleted() {
        TableBooking booking = baseBooking(BookingStatusEnum.CHECKED_IN);
        booking.setCheckInAt(null);
        booking.setCheckOutAt(null);

        BookingTransitionContext context = BookingTransitionContext.builder()
                .booking(booking)
                .targetStatus(BookingStatusEnum.COMPLETED)
                .allowNoopTransition(true)
                .build();

        TableBooking result = bookingStateMachine.transition(context);

        assertEquals(BookingStatusEnum.COMPLETED.getCode(), result.getBookingStatus());
        assertNotNull(result.getCheckInAt());
        assertNotNull(result.getCheckOutAt());
    }

    @Test
    void shouldInitializeBookingWithPendingOrConfirmedOnly() {
        TableBooking pending = baseBooking(BookingStatusEnum.PENDING);
        pending.setBookingStatus(null);

        TableBooking result = bookingStateMachine.initialize(pending, BookingStatusEnum.PENDING);
        assertEquals(BookingStatusEnum.PENDING.getCode(), result.getBookingStatus());

        TableBooking invalidInitial = baseBooking(BookingStatusEnum.PENDING);
        invalidInitial.setBookingStatus(null);

        BookingStateTransitionException ex = assertThrows(
                BookingStateTransitionException.class,
                () -> bookingStateMachine.initialize(invalidInitial, BookingStatusEnum.CHECKED_IN)
        );
        assertTrue(ex.getMessage().startsWith("BOOKING_TRANSITION_NOT_ALLOWED"));
    }

    private TableBooking baseBooking(BookingStatusEnum status) {
        TableEntity table = TableEntity.builder()
                .id(1)
                .active(true)
                .build();

        return TableBooking.builder()
                .id(100)
                .table(table)
                .active(true)
                .bookingStatus(status.getCode())
                .expectedArriveTime(LocalDateTime.now().plusMinutes(45))
                .expectedCheckOut(LocalDateTime.now().plusHours(2))
                .build();
    }

    private void adaptBookingForTransition(TableBooking booking, BookingStatusEnum from, BookingStatusEnum to) {
        if (from == BookingStatusEnum.CHECKED_IN) {
            booking.setCheckInAt(LocalDateTime.now().minusMinutes(5));
        }

        if (from == BookingStatusEnum.CONFIRMED && to == BookingStatusEnum.EXPIRED) {
            booking.setExpectedArriveTime(LocalDateTime.now().minusMinutes(31));
            booking.setExpectedCheckOut(LocalDateTime.now().plusMinutes(89));
            booking.setCheckInAt(null);
        }

        if (from == BookingStatusEnum.CONFIRMED && to == BookingStatusEnum.CHECKED_IN) {
            booking.setExpectedArriveTime(LocalDateTime.now());
            booking.setExpectedCheckOut(LocalDateTime.now().plusHours(2));
        }

        if ((from == BookingStatusEnum.PENDING || from == BookingStatusEnum.CONFIRMED)
                && to == BookingStatusEnum.CANCELLED) {
            booking.setExpectedArriveTime(LocalDateTime.now().plusMinutes(20));
        }
    }

    private static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(BookingStatusEnum.PENDING, BookingStatusEnum.CONFIRMED),
                Arguments.of(BookingStatusEnum.PENDING, BookingStatusEnum.CANCELLED),
                Arguments.of(BookingStatusEnum.CONFIRMED, BookingStatusEnum.CHECKED_IN),
                Arguments.of(BookingStatusEnum.CONFIRMED, BookingStatusEnum.CANCELLED),
                Arguments.of(BookingStatusEnum.CONFIRMED, BookingStatusEnum.EXPIRED),
                Arguments.of(BookingStatusEnum.CHECKED_IN, BookingStatusEnum.COMPLETED),
                Arguments.of(BookingStatusEnum.CHECKED_IN, BookingStatusEnum.CANCELLED)
        );
    }

    private static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(BookingStatusEnum.PENDING, BookingStatusEnum.CHECKED_IN),
                Arguments.of(BookingStatusEnum.PENDING, BookingStatusEnum.COMPLETED),
                Arguments.of(BookingStatusEnum.CONFIRMED, BookingStatusEnum.COMPLETED),
                Arguments.of(BookingStatusEnum.CHECKED_IN, BookingStatusEnum.CONFIRMED),
                Arguments.of(BookingStatusEnum.CANCELLED, BookingStatusEnum.CONFIRMED),
                Arguments.of(BookingStatusEnum.COMPLETED, BookingStatusEnum.CANCELLED),
                Arguments.of(BookingStatusEnum.EXPIRED, BookingStatusEnum.CHECKED_IN)
        );
    }
}
