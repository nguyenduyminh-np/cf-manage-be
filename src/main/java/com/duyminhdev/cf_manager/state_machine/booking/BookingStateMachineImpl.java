package com.duyminhdev.cf_manager.state_machine.booking;

import com.duyminhdev.cf_manager.constant.BookingStateMachineConstant;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.exceptions.BookingStateTransitionException;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import com.duyminhdev.cf_manager.validator.booking.BookingRuleValidatorChain;
import com.duyminhdev.cf_manager.validator.booking.BookingValidationContext;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookingStateMachineImpl implements BookingStateMachine {

    private static final Map<BookingStatusEnum, Set<BookingStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatusEnum.PENDING, Set.of(
                    BookingStatusEnum.CONFIRMED,
                    BookingStatusEnum.CANCELLED
            ),
            BookingStatusEnum.CONFIRMED, Set.of(
                    BookingStatusEnum.CHECKED_IN,
                    BookingStatusEnum.CANCELLED,
                    BookingStatusEnum.EXPIRED
            ),
            BookingStatusEnum.CHECKED_IN, Set.of(
                    BookingStatusEnum.COMPLETED,
                    BookingStatusEnum.CANCELLED
            ),
            BookingStatusEnum.CANCELLED, Set.of(),
            BookingStatusEnum.COMPLETED, Set.of(),
            BookingStatusEnum.EXPIRED, Set.of()
    );

    private final TableRepository tableRepository;
    private final ServiceSupport serviceSupport;
    private final BookingRuleValidatorChain bookingRuleValidatorChain;

    @Override
    public TableBooking initialize(TableBooking booking, BookingStatusEnum initialStatus) {
        if (booking == null) {
            throw guardFailed("booking is required");
        }
        if (initialStatus == null) {
            throw guardFailed("initialStatus is required");
        }

        ensureActiveBooking(booking);
        lockTable(booking);

        if (initialStatus != BookingStatusEnum.PENDING && initialStatus != BookingStatusEnum.CONFIRMED) {
            throw transitionNotAllowed("initial state must be PENDING_CONFIRMATION or CONFIRMED");
        }

        serviceSupport.validateBookingTimes(
                booking.getExpectedArriveTime(),
                booking.getExpectedCheckOut(),
                booking.getCheckInAt(),
                booking.getCheckOutAt()
        );

        BookingValidationContext validationContext = BookingValidationContext.builder()
            .booking(booking)
            .table(booking.getTable())
            .targetStatus(initialStatus)
            .expectedArriveTime(booking.getExpectedArriveTime())
            .expectedCheckOut(booking.getExpectedCheckOut())
            .excludeBookingId(booking.getId())
            .now(LocalDateTime.now())
            .build();

        bookingRuleValidatorChain.validate(BookingValidationUseCase.CREATE_BOOKING, validationContext);

        if (initialStatus == BookingStatusEnum.CONFIRMED) {
            bookingRuleValidatorChain.validate(BookingValidationUseCase.CONFIRM_BOOKING, validationContext);
        }

        booking.setBookingStatus(initialStatus.getCode());
        return booking;
    }

    @Override
    public TableBooking transition(BookingTransitionContext context) {
        validateContext(context);

        TableBooking booking = context.getBooking();
        ensureActiveBooking(booking);
        lockTable(booking);

        BookingStatusEnum currentStatus = BookingStatusEnum.fromCode(booking.getBookingStatus());
        BookingStatusEnum targetStatus = context.getTargetStatus();

        if (currentStatus != targetStatus && !isAllowedTransition(currentStatus, targetStatus)) {
            throw transitionNotAllowed(currentStatus.getCode() + " -> " + targetStatus.getCode());
        }

        if (currentStatus == targetStatus && !context.isAllowNoopTransition()) {
            throw transitionNotAllowed("same-state transition is disabled for " + targetStatus.getCode());
        }

        LocalDateTime resolvedCheckInAt = resolveCheckInAt(booking, targetStatus, context.getRequestedCheckInAt());
        LocalDateTime resolvedCheckOutAt = resolveCheckOutAt(booking, targetStatus, context.getRequestedCheckOutAt());

        BookingValidationContext validationContext = BookingValidationContext.builder()
            .booking(booking)
            .table(booking.getTable())
            .currentStatus(currentStatus)
            .targetStatus(targetStatus)
            .requestedCheckInAt(resolvedCheckInAt)
            .requestedCheckOutAt(resolvedCheckOutAt)
            .expectedArriveTime(booking.getExpectedArriveTime())
            .expectedCheckOut(booking.getExpectedCheckOut())
            .excludeBookingId(booking.getId())
            .now(LocalDateTime.now())
            .force(context.isForce())
            .build();

        revalidateBusinessRules(currentStatus, targetStatus, booking, resolvedCheckInAt, resolvedCheckOutAt, validationContext);

        booking.setBookingStatus(targetStatus.getCode());
        booking.setCheckInAt(resolvedCheckInAt);
        booking.setCheckOutAt(resolvedCheckOutAt);
        return booking;
    }

    private void validateContext(BookingTransitionContext context) {
        if (context == null) {
            throw guardFailed("transition context is required");
        }
        if (context.getBooking() == null) {
            throw guardFailed("booking is required");
        }
        if (context.getTargetStatus() == null) {
            throw guardFailed("targetStatus is required");
        }
    }

    private void ensureActiveBooking(TableBooking booking) {
        if (!Boolean.TRUE.equals(booking.getActive())) {
            throw guardFailed("booking is inactive (bookingId=" + booking.getId() + ")");
        }
    }

    private void lockTable(TableBooking booking) {
        if (booking.getTable() == null || booking.getTable().getId() == null) {
            throw guardFailed("tableId is required for transition lock");
        }

        Integer tableId = booking.getTable().getId();
        tableRepository.findByIdAndActiveTrueForUpdate(tableId)
                .orElseThrow(() -> guardFailed("table not found or inactive (tableId=" + tableId + ")"));
    }

    private boolean isAllowedTransition(BookingStatusEnum currentStatus, BookingStatusEnum targetStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus);
    }

    private LocalDateTime resolveCheckInAt(
            TableBooking booking,
            BookingStatusEnum targetStatus,
            LocalDateTime requestedCheckInAt
    ) {
        LocalDateTime resolved = requestedCheckInAt != null ? requestedCheckInAt : booking.getCheckInAt();

        if (targetStatus == BookingStatusEnum.CHECKED_IN && resolved == null) {
            return LocalDateTime.now();
        }

        if (targetStatus == BookingStatusEnum.COMPLETED && resolved == null) {
            return LocalDateTime.now();
        }

        return resolved;
    }

    private LocalDateTime resolveCheckOutAt(
            TableBooking booking,
            BookingStatusEnum targetStatus,
            LocalDateTime requestedCheckOutAt
    ) {
        LocalDateTime resolved = requestedCheckOutAt != null ? requestedCheckOutAt : booking.getCheckOutAt();

        if (targetStatus == BookingStatusEnum.COMPLETED && resolved == null) {
            return LocalDateTime.now();
        }

        return resolved;
    }

    private void revalidateBusinessRules(
            BookingStatusEnum currentStatus,
            BookingStatusEnum targetStatus,
            TableBooking booking,
            LocalDateTime resolvedCheckInAt,
            LocalDateTime resolvedCheckOutAt,
            BookingValidationContext validationContext
    ) {
        serviceSupport.validateBookingTimes(
                booking.getExpectedArriveTime(),
                booking.getExpectedCheckOut(),
                resolvedCheckInAt,
                resolvedCheckOutAt
        );

        if (targetStatus == BookingStatusEnum.CONFIRMED) {
            bookingRuleValidatorChain.validate(BookingValidationUseCase.CONFIRM_BOOKING, validationContext);
        }

        if (targetStatus == BookingStatusEnum.CHECKED_IN) {
            bookingRuleValidatorChain.validate(BookingValidationUseCase.CHECK_IN, validationContext);
        }

        if (targetStatus == BookingStatusEnum.CANCELLED) {
            validateCancelBeforeArrive(currentStatus, booking);
        }

        if (targetStatus == BookingStatusEnum.EXPIRED) {
            validateNoShowExpire(booking, resolvedCheckInAt);
        }

        if (targetStatus == BookingStatusEnum.COMPLETED) {
            validateCompletedState(resolvedCheckInAt, resolvedCheckOutAt);
        }
    }

    private void validateCancelBeforeArrive(BookingStatusEnum currentStatus, TableBooking booking) {
        if (currentStatus == BookingStatusEnum.PENDING || currentStatus == BookingStatusEnum.CONFIRMED) {
            LocalDateTime arrive = booking.getExpectedArriveTime();
            if (arrive != null && LocalDateTime.now().isAfter(arrive)) {
                throw ruleViolation("cannot cancel booking after expected arrive time");
            }
        }
    }

    private void validateNoShowExpire(TableBooking booking, LocalDateTime resolvedCheckInAt) {
        LocalDateTime arrive = booking.getExpectedArriveTime();
        if (arrive == null) {
            throw ruleViolation("expectedArriveTime is required before EXPIRED");
        }

        LocalDateTime threshold = arrive.plusMinutes(30);
        if (!LocalDateTime.now().isAfter(threshold)) {
            throw ruleViolation("booking is not eligible for EXPIRED before no-show threshold");
        }

        if (resolvedCheckInAt != null) {
            throw ruleViolation("checked-in booking cannot transition to EXPIRED");
        }
    }

    private void validateCompletedState(LocalDateTime resolvedCheckInAt, LocalDateTime resolvedCheckOutAt) {
        if (resolvedCheckInAt == null) {
            throw ruleViolation("checkInAt is required for COMPLETED");
        }

        if (resolvedCheckOutAt == null) {
            throw ruleViolation("checkOutAt is required for COMPLETED");
        }
    }

    private BookingStateTransitionException guardFailed(String message) {
        return new BookingStateTransitionException(BookingStateMachineConstant.PREFIX_GUARD_FAILED + message);
    }

    private BookingStateTransitionException transitionNotAllowed(String message) {
        return new BookingStateTransitionException(BookingStateMachineConstant.PREFIX_NOT_ALLOWED + message);
    }

    private BookingStateTransitionException ruleViolation(String message) {
        return new BookingStateTransitionException(BookingStateMachineConstant.PREFIX_RULE_VIOLATION + message);
    }
}
