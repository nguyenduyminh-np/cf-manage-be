package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.exceptions.BookingStateTransitionException;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingValidatorsUnitTest {

    @Mock
    private TableBookingRepository tableBookingRepository;

    @Test
        void advanceBookingRule_shouldAllowWhenTwoHourRuleTemporarilyDisabled() {
        AdvanceBookingValidator validator = new AdvanceBookingValidator();

        BookingValidationContext context = baseContextBuilder()
                .now(at(2026, 4, 10, 10, 0))
                .expectedArriveTime(at(2026, 4, 10, 11, 30))
                .build();

                assertDoesNotThrow(() -> validator.validate(context));
        }

        @Test
        void expectedArriveTimeWindowRule_shouldRejectWhenOutsideBusinessHours() {
                ExpectedArriveTimeWindowValidator validator = new ExpectedArriveTimeWindowValidator();

                BookingValidationContext context = baseContextBuilder()
                                // 21:00 at Asia/Ho_Chi_Minh equals 14:00 UTC
                                .expectedArriveTime(at(2026, 4, 10, 14, 0))
                                .build();

                BookingStateTransitionException ex = assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
                assertTrue(ex.getMessage().contains("[RULE_01_EXPECTED_ARRIVE_WINDOW][ExpectedArriveTimeWindowValidator]"));
        }

        @Test
        void expectedArriveTimeWindowRule_shouldAllowWhenInsideBusinessHours() {
                ExpectedArriveTimeWindowValidator validator = new ExpectedArriveTimeWindowValidator();

                BookingValidationContext context = baseContextBuilder()
                                // 20:00 at Asia/Ho_Chi_Minh equals 13:00 UTC
                                .expectedArriveTime(at(2026, 4, 10, 13, 0))
                                .build();

                assertDoesNotThrow(() -> validator.validate(context));
    }

    @Test
    void durationRule_shouldRejectWhenCheckoutNotAfterArrive() {
        DurationValidator validator = new DurationValidator();

        Instant at = at(2026, 4, 10, 12, 0);
        BookingValidationContext context = baseContextBuilder()
                .expectedArriveTime(at)
                .expectedCheckOut(at)
                .build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void noConflictRule_shouldRejectOverlap() {
        NoConflictValidator validator = new NoConflictValidator(tableBookingRepository);
        when(tableBookingRepository.existsConflictBookingOnTable(anyInt(), any(), any(), anyCollection(), any()))
                .thenReturn(true);

        BookingValidationContext context = baseContextBuilder().build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

        @Test
        void noConflictRule_shouldSupportWalkInUseCase() {
                NoConflictValidator validator = new NoConflictValidator(tableBookingRepository);

                assertTrue(validator.supports(BookingValidationUseCase.WALK_IN_BOOKING));
        }

    @Test
    void depositRule_shouldRejectUnpaidDeposit() {
        DepositValidator validator = new DepositValidator();
        TableBooking booking = baseBooking();
        booking.setDepositAmount(BigDecimal.valueOf(100_000));
        booking.setDepositPaid(false);

        BookingValidationContext context = baseContextBuilder().booking(booking).build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void earlyArrivalRule_shouldRequireForceWhenConflictExists() {
        EarlyArrivalValidator validator = new EarlyArrivalValidator(tableBookingRepository);

        TableBooking next = baseBooking();
        next.setId(999);
        next.setExpectedArriveTime(at(2026, 4, 10, 12, 30));

        when(tableBookingRepository.findConfirmedBookingsFromTime(anyInt(), any(), anyString(), any()))
                .thenReturn(List.of(next));

        BookingValidationContext context = baseContextBuilder()
                .now(at(2026, 4, 10, 10, 0))
                .requestedCheckInAt(at(2026, 4, 10, 10, 30))
                .expectedArriveTime(at(2026, 4, 10, 12, 0))
                .expectedCheckOut(at(2026, 4, 10, 14, 0))
                .force(false)
                .build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void lateArrivalRule_shouldRejectOverThirtyMinutes() {
        LateArrivalValidator validator = new LateArrivalValidator();

        BookingValidationContext context = baseContextBuilder()
                .requestedCheckInAt(at(2026, 4, 10, 12, 31))
                .expectedArriveTime(at(2026, 4, 10, 12, 0))
                .build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void extensionRule_shouldRejectNearNextBooking() {
        ExtensionValidator validator = new ExtensionValidator(tableBookingRepository);

        TableBooking next = baseBooking();
        next.setExpectedArriveTime(at(2026, 4, 10, 14, 20));

        when(tableBookingRepository.findConfirmedBookingsFromTime(anyInt(), any(), anyString(), any()))
                .thenReturn(List.of(next));

        BookingValidationContext context = baseContextBuilder()
                .now(at(2026, 4, 10, 13, 0))
                .expectedCheckOut(at(2026, 4, 10, 14, 0))
                .build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

        @Test
        void walkInGuardRule_shouldBlockWhenIntervalConflicts() {
        WalkInGuardValidator validator = new WalkInGuardValidator(tableBookingRepository);
        when(tableBookingRepository.existsConflictBookingOnTable(anyInt(), any(), any(), anyCollection(), any()))
                .thenReturn(true);

        BookingValidationContext context = baseContextBuilder().build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void pendingConfirmationAdvisory_shouldAppendWarning() {
        PendingConfirmationAdvisoryValidator validator = new PendingConfirmationAdvisoryValidator(tableBookingRepository);
        when(tableBookingRepository.existsActiveBookingOnTableFromTimeAndStatuses(anyInt(), any(), anyCollection(), any()))
                .thenReturn(true);

        BookingValidationContext context = baseContextBuilder().build();
        validator.validate(context);

        assertFalse(context.getWarnings().isEmpty());
    }

    @Test
    void ownershipRule_shouldRejectWhenBookedByOtherBooking() {
        BookingOwnershipValidator validator = new BookingOwnershipValidator(tableBookingRepository);

        TableBooking owner = baseBooking();
        owner.setId(777);

        when(tableBookingRepository.findConfirmedBookingsFromTime(anyInt(), any(), anyString(), any()))
                .thenReturn(List.of(owner));

        TableBooking booking = baseBooking();
        booking.setId(100);
        booking.getTable().setTableStatus(TableStatusEnum.BOOKED.getCode());

        BookingValidationContext context = baseContextBuilder()
                .booking(booking)
                .expectedArriveTime(at(2026, 4, 10, 12, 0))
                .requestedCheckInAt(at(2026, 4, 10, 11, 0))
                .build();

        assertThrows(BookingStateTransitionException.class, () -> validator.validate(context));
    }

    @Test
    void walkInPreAssignWarning_shouldAppendWarning() {
        WalkInPreAssignWarningValidator validator = new WalkInPreAssignWarningValidator(tableBookingRepository);
        when(tableBookingRepository.existsActiveBookingOnTableInWindowAndStatuses(anyInt(), any(), any(), anyCollection(), any()))
                .thenReturn(true);

        BookingValidationContext context = baseContextBuilder().build();
        validator.validate(context);

        assertTrue(context.getWarnings().size() == 1);
                assertTrue(context.getWarnings().getFirst().contains("[RULE_16_WALK_IN_PRE_ASSIGN_WARNING][WalkInPreAssignWarningValidator]"));
    }

    private BookingValidationContext.BookingValidationContextBuilder baseContextBuilder() {
        TableBooking booking = baseBooking();
        return BookingValidationContext.builder()
                .booking(booking)
                .table(booking.getTable())
                .currentStatus(BookingStatusEnum.PENDING)
                .targetStatus(BookingStatusEnum.CONFIRMED)
                .now(at(2026, 4, 10, 10, 0))
                .expectedArriveTime(at(2026, 4, 10, 12, 0))
                .expectedCheckOut(at(2026, 4, 10, 14, 0))
                .excludeBookingId(booking.getId())
                .force(false);
    }

    private static Instant at(int year, int month, int day, int hour, int minute) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, 0).toInstant(java.time.ZoneOffset.UTC);
    }

    private TableBooking baseBooking() {
        TableEntity table = TableEntity.builder()
                .id(1)
                .tableStatus(TableStatusEnum.AVAILABLE.getCode())
                .active(true)
                .build();

        return TableBooking.builder()
                .id(100)
                .table(table)
                .active(true)
                .bookingStatus(BookingStatusEnum.PENDING.getCode())
                .expectedArriveTime(at(2026, 4, 10, 12, 0))
                .expectedCheckOut(at(2026, 4, 10, 14, 0))
                .build();
    }
}
