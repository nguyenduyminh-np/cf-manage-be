package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import com.duyminhdev.cf_manager.event.booking.BookingDomainEventPublisher;
import com.duyminhdev.cf_manager.event.booking.BookingMutationType;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.mapper.TableBookingMapper;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.state_machine.booking.BookingStateMachine;
import com.duyminhdev.cf_manager.state_machine.booking.BookingTransitionContext;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import com.duyminhdev.cf_manager.validator.booking.BookingRuleValidatorChain;
import com.duyminhdev.cf_manager.validator.booking.BookingValidationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingUseCaseServiceImpl implements BookingUseCaseService {

    private static final long DEFAULT_WALK_IN_DURATION_HOURS = 2L;

    private final TableBookingRepository tableBookingRepository;
    private final TableBookingMapper tableBookingMapper;
    private final ServiceSupport serviceSupport;
    private final BookingStateMachine bookingStateMachine;
    private final BookingLockService bookingLockService;
    private final BookingRuleValidatorChain bookingRuleValidatorChain;
    private final BookingDomainEventPublisher bookingDomainEventPublisher;

    @Override
    @Transactional
    public TableBooking createBooking(TableBookingCreateRequestDTO request) {
        Integer tableId = requirePositive(request.getTableId(), "Mã bàn (tableId) là bắt buộc");
        return bookingLockService.executeWithTableLock(tableId, () -> {
            serviceSupport.validateExpectedArriveTime(request.getExpectedArriveTime());
            serviceSupport.validateBookingTimes(
                    request.getExpectedArriveTime(),
                    request.getExpectedCheckOut(),
                    null,
                    null
            );

            String bookingStatus = request.getBookingStatus();
            if (bookingStatus == null || bookingStatus.isBlank()) {
                bookingStatus = BookingStatusEnum.PENDING.getCode();
            }

            BookingStatusEnum initialStatus = BookingStatusEnum.fromCode(bookingStatus);
            serviceSupport.validateBookingStatusCode(initialStatus.getCode());

            TableEntity table = serviceSupport.getActiveTable(tableId);
            Account account = serviceSupport.getCurrentAccount();

            TableBooking entity = tableBookingMapper.toNewEntity(request);
            entity.setTable(table);
            entity.setAccount(account);
            entity.setCreatedAt(Instant.now());
            entity.setActive(true);

            bookingStateMachine.initialize(entity, initialStatus);

            TableBooking saved = tableBookingRepository.save(entity);
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.CREATE, saved.getId(), saved.getTable().getId());
            return saved;
        });
    }

    @Override
    @Transactional
    public TableBooking confirmBooking(Integer bookingId) {
        return transitionUseCase(
                bookingId,
                BookingStatusEnum.CONFIRMED,
                null,
                null,
                false,
                false,
                BookingMutationType.CONFIRM
        );
    }

    @Override
    @Transactional
    public TableBooking checkIn(Integer bookingId, Instant requestedCheckInAt, boolean force) {
        return transitionUseCase(
                bookingId,
                BookingStatusEnum.CHECKED_IN,
                requestedCheckInAt,
                null,
                force,
                false,
                BookingMutationType.CHECK_IN
        );
    }

    @Override
    @Transactional
    public TableBooking checkOut(Integer bookingId, Instant requestedCheckOutAt) {
        return transitionUseCase(
                bookingId,
                BookingStatusEnum.COMPLETED,
                null,
                requestedCheckOutAt,
                false,
                false,
                BookingMutationType.CHECK_OUT
        );
    }

    @Override
    @Transactional
    public TableBooking cancelBooking(Integer bookingId) {
        return transitionUseCase(
                bookingId,
                BookingStatusEnum.CANCELLED,
                null,
                null,
                false,
                false,
                BookingMutationType.CANCEL
        );
    }

    @Override
    @Transactional
    public TableBooking expireBooking(Integer bookingId) {
        return transitionUseCase(
                bookingId,
                BookingStatusEnum.EXPIRED,
                null,
                null,
                false,
                false,
                BookingMutationType.EXPIRE
        );
    }

    @Override
    @Transactional
    public TableBooking extendBooking(Integer bookingId, Instant newExpectedCheckOut, boolean force) {
        requirePositive(bookingId, "Mã đặt bàn (bookingId) là bắt buộc");
        if (newExpectedCheckOut == null) {
            throw new InvalidDataException("Thời gian trả bàn mới (newExpectedCheckOut) là bắt buộc");
        }

        TableBooking beforeLock = loadActiveBooking(bookingId);
        Integer tableId = requireTableId(beforeLock);

        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableBooking booking = loadActiveBooking(bookingId);
            BookingStatusEnum currentStatus = BookingStatusEnum.fromCode(booking.getBookingStatus());

            if (!currentStatus.isCheckedIn()) {
                throw new InvalidDataException("Gia hạn chỉ áp dụng cho đặt bàn đang trong trạng thái ĐÃ_CHECK_IN");
            }

            serviceSupport.validateBookingTimes(
                    booking.getExpectedArriveTime(),
                    newExpectedCheckOut,
                    booking.getCheckInAt(),
                    booking.getCheckOutAt()
            );

            BookingValidationContext validationContext = BookingValidationContext.builder()
                    .booking(booking)
                    .table(booking.getTable())
                    .currentStatus(currentStatus)
                    .targetStatus(currentStatus)
                    .requestedCheckInAt(booking.getCheckInAt())
                    .requestedCheckOutAt(booking.getCheckOutAt())
                    .expectedArriveTime(booking.getExpectedArriveTime())
                    .expectedCheckOut(newExpectedCheckOut)
                    .excludeBookingId(booking.getId())
                    .now(Instant.now())
                    .force(force)
                    .build();

            bookingRuleValidatorChain.validate(BookingValidationUseCase.EXTEND_BOOKING, validationContext);

            booking.setExpectedCheckOut(newExpectedCheckOut);
            TableBooking saved = tableBookingRepository.save(booking);

            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.EXTEND, saved.getId(), saved.getTable().getId());
            return saved;
        });
    }

    @Override
    @Transactional
    public TableBooking createWalkIn(TableBookingCreateRequestDTO request, boolean force) {
        Integer tableId = requirePositive(request.getTableId(), "Mã bàn (tableId) là bắt buộc");

        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableEntity table = serviceSupport.getActiveTable(tableId);
            Account account = serviceSupport.getCurrentAccount();
            Instant now = Instant.now();

            TableBooking walkInBooking = buildWalkInBooking(request, table, account, now);

            BookingValidationContext validationContext = BookingValidationContext.builder()
                    .booking(walkInBooking)
                    .table(table)
                    .currentStatus(BookingStatusEnum.CONFIRMED)
                    .targetStatus(BookingStatusEnum.CHECKED_IN)
                    .requestedCheckInAt(now)
                    .expectedArriveTime(walkInBooking.getExpectedArriveTime())
                    .expectedCheckOut(walkInBooking.getExpectedCheckOut())
                    .excludeBookingId(null)
                    .now(now)
                    .force(force)
                    .build();

            bookingRuleValidatorChain.validate(BookingValidationUseCase.WALK_IN_BOOKING, validationContext);

            bookingStateMachine.transition(BookingTransitionContext.builder()
                    .booking(walkInBooking)
                    .targetStatus(BookingStatusEnum.CHECKED_IN)
                    .requestedCheckInAt(now)
                    .allowNoopTransition(false)
                    .force(force)
                    .build());

            TableBooking saved = tableBookingRepository.save(walkInBooking);
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.WALK_IN, saved.getId(), saved.getTable().getId());
            return saved;
        });
    }

    @Override
    @Transactional
    public TableBooking createWalkInFromLateArrival(Integer lateBookingId, TableBookingCreateRequestDTO walkInRequest, boolean force) {
        requirePositive(lateBookingId, "Mã đặt bàn trễ (lateBookingId) là bắt buộc");

        TableBooking lateBookingBeforeLock = loadActiveBooking(lateBookingId);
        Integer oldTableId = requireTableId(lateBookingBeforeLock);

        TableBookingCreateRequestDTO safeWalkInRequest = walkInRequest != null ? walkInRequest : new TableBookingCreateRequestDTO();
        Integer newTableId = safeWalkInRequest.getTableId() != null ? safeWalkInRequest.getTableId() : oldTableId;

        return bookingLockService.executeWithTableLocks(List.of(oldTableId, newTableId), () -> {
            TableBooking lateBooking = loadActiveBooking(lateBookingId);
            validateLateArrivalNoShow(lateBooking, Instant.now());

            bookingStateMachine.transition(BookingTransitionContext.builder()
                    .booking(lateBooking)
                    .targetStatus(BookingStatusEnum.EXPIRED)
                    .allowNoopTransition(false)
                    .build());
            tableBookingRepository.save(lateBooking);
            bookingDomainEventPublisher.publish(BookingMutationType.EXPIRE, lateBooking.getId(), lateBooking.getTable().getId());

            safeWalkInRequest.setTableId(newTableId);
            if (safeWalkInRequest.getCustomerName() == null) {
                safeWalkInRequest.setCustomerName(lateBooking.getCustomerName());
            }
            if (safeWalkInRequest.getPhoneNumber() == null) {
                safeWalkInRequest.setPhoneNumber(lateBooking.getPhoneNumber());
            }

            TableEntity newTable = serviceSupport.getActiveTable(newTableId);
            Account account = serviceSupport.getCurrentAccount();
            Instant now = Instant.now();

            TableBooking walkInBooking = buildWalkInBooking(safeWalkInRequest, newTable, account, now);

            BookingValidationContext validationContext = BookingValidationContext.builder()
                    .booking(walkInBooking)
                    .table(newTable)
                    .currentStatus(BookingStatusEnum.CONFIRMED)
                    .targetStatus(BookingStatusEnum.CHECKED_IN)
                    .requestedCheckInAt(now)
                    .expectedArriveTime(walkInBooking.getExpectedArriveTime())
                    .expectedCheckOut(walkInBooking.getExpectedCheckOut())
                    .excludeBookingId(null)
                    .now(now)
                    .force(force)
                    .build();

            bookingRuleValidatorChain.validate(BookingValidationUseCase.LATE_ARRIVAL_WALK_IN, validationContext);

            bookingStateMachine.transition(BookingTransitionContext.builder()
                    .booking(walkInBooking)
                    .targetStatus(BookingStatusEnum.CHECKED_IN)
                    .requestedCheckInAt(now)
                    .allowNoopTransition(false)
                    .force(force)
                    .build());

            TableBooking savedWalkIn = tableBookingRepository.save(walkInBooking);

            serviceSupport.recomputeAndSyncTableStatus(oldTableId);
            if (!oldTableId.equals(newTableId)) {
                serviceSupport.recomputeAndSyncTableStatus(newTableId);
            }

            bookingDomainEventPublisher.publish(
                    BookingMutationType.LATE_ARRIVAL_WALK_IN,
                    savedWalkIn.getId(),
                    savedWalkIn.getTable().getId()
            );

            return savedWalkIn;
        });
    }

    @Override
    @Transactional
    public TableBooking cancelBookingNoOrderTimeout(Integer bookingId, Instant now) {
        requirePositive(bookingId, "Mã đặt bàn (bookingId) là bắt buộc");

        TableBooking beforeLock = loadActiveBooking(bookingId);
        Integer tableId = requireTableId(beforeLock);

        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableBooking booking = loadActiveBooking(bookingId);
            BookingStatusEnum currentStatus = BookingStatusEnum.fromCode(booking.getBookingStatus());
            if (!currentStatus.isCheckedIn()) {
                throw new InvalidDataException("Hủy đặt bàn do quá thời gian không gọi món chỉ áp dụng cho đặt bàn đang ĐÃ_CHECK_IN");
            }

            Instant effectiveNow = now != null ? now : Instant.now();
            if (booking.getCheckInAt() == null || effectiveNow.isBefore(booking.getCheckInAt().plus(Duration.ofMinutes(20)))) {
                throw new InvalidDataException("Đặt bàn chưa đủ điều kiện để hủy do quá thời gian không gọi món");
            }

            if (serviceSupport.hasUnfinishedOrders(tableId)) {
                throw new InvalidDataException("Đặt bàn có món ăn đang xử lý, không thể hủy do quá thời gian");
            }

            bookingStateMachine.transition(BookingTransitionContext.builder()
                    .booking(booking)
                    .targetStatus(BookingStatusEnum.CANCELLED)
                    .allowNoopTransition(false)
                    .build());

                // Rule 14: this path is refund-oriented, therefore mark as non-forfeit.
                booking.setDepositForfeited(false);

                // Actual payment/refund transaction is handled in dedicated payment/deposit module.
            TableBooking saved = tableBookingRepository.save(booking);
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(
                    BookingMutationType.CANCEL_NO_ORDER_TIMEOUT,
                    saved.getId(),
                    saved.getTable().getId()
            );
            return saved;
        });
    }

    @Override
    @Transactional
    public TableBooking markDepositPaid(Integer bookingId, BigDecimal depositAmount, String depositTxnRef, Instant paidAt) {
        requirePositive(bookingId, "Mã đặt bàn (bookingId) là bắt buộc");

        TableBooking beforeLock = loadActiveBooking(bookingId);
        Integer tableId = requireTableId(beforeLock);

        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableBooking booking = loadActiveBooking(bookingId);
            BookingStatusEnum currentStatus = BookingStatusEnum.fromCode(booking.getBookingStatus());
            if (currentStatus.isCancelled() || currentStatus.isCompleted() || currentStatus == BookingStatusEnum.EXPIRED) {
                throw new InvalidDataException("Không thể thực hiện đặt cọc khi đặt bàn đã ở trạng thái kết thúc");
            }

            BigDecimal effectiveAmount = depositAmount != null ? depositAmount : booking.getDepositAmount();
            if (effectiveAmount == null || effectiveAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidDataException("Số tiền đặt cọc (depositAmount) phải lớn hơn 0");
            }

            booking.setDepositAmount(effectiveAmount);
            booking.setDepositPaid(true);
            booking.setDepositPaidAt(paidAt != null ? paidAt : Instant.now());
            booking.setDepositForfeited(false);
            booking.setDepositTxnRef(StringUtils.hasText(depositTxnRef) ? depositTxnRef.trim() : booking.getDepositTxnRef());

            if (currentStatus.isPending()) {
                bookingStateMachine.transition(BookingTransitionContext.builder()
                        .booking(booking)
                        .targetStatus(BookingStatusEnum.CONFIRMED)
                        .allowNoopTransition(false)
                        .build());
            }

            TableBooking saved = tableBookingRepository.save(booking);
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.DEPOSIT, saved.getId(), saved.getTable().getId());
            return saved;
        });
    }

    private TableBooking transitionUseCase(
            Integer bookingId,
            BookingStatusEnum targetStatus,
            Instant requestedCheckInAt,
            Instant requestedCheckOutAt,
            boolean force,
            boolean allowNoop,
            BookingMutationType mutationType
    ) {
        requirePositive(bookingId, "Mã đặt bàn (bookingId) là bắt buộc");
        TableBooking beforeLock = loadActiveBooking(bookingId);
        Integer tableId = requireTableId(beforeLock);

        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableBooking booking = loadActiveBooking(bookingId);

            bookingStateMachine.transition(BookingTransitionContext.builder()
                    .booking(booking)
                    .targetStatus(targetStatus)
                    .requestedCheckInAt(requestedCheckInAt)
                    .requestedCheckOutAt(requestedCheckOutAt)
                    .allowNoopTransition(allowNoop)
                    .force(force)
                    .build());

            TableBooking saved = tableBookingRepository.save(booking);
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(mutationType, saved.getId(), saved.getTable().getId());
            return saved;
        });
    }

    private TableBooking buildWalkInBooking(
            TableBookingCreateRequestDTO request,
            TableEntity table,
            Account account,
            Instant now
    ) {
        TableBookingCreateRequestDTO safeRequest = request != null ? request : new TableBookingCreateRequestDTO();
        TableBooking booking = tableBookingMapper.toNewEntity(safeRequest);
        if (booking == null) {
            booking = new TableBooking();
        }

        booking.setTable(table);
        booking.setAccount(account);
        booking.setCreatedAt(now);
        booking.setActive(true);
        booking.setExpectedArriveTime(now);
        booking.setExpectedCheckOut(resolveWalkInCheckOut(safeRequest.getExpectedCheckOut(), now));
        booking.setBookingStatus(BookingStatusEnum.CONFIRMED.getCode());
        booking.setCheckInAt(null);
        booking.setCheckOutAt(null);

        return booking;
    }

    private Instant resolveWalkInCheckOut(Instant requestedCheckOut, Instant now) {
        if (requestedCheckOut == null) {
            return now.plus(Duration.ofHours(DEFAULT_WALK_IN_DURATION_HOURS));
        }
        if (!requestedCheckOut.isAfter(now)) {
            throw new InvalidDataException("Thời gian trả bàn walk-in phải sau thời điểm hiện tại");
        }
        return requestedCheckOut;
    }

    private void validateLateArrivalNoShow(TableBooking lateBooking, Instant now) {
        BookingStatusEnum status = BookingStatusEnum.fromCode(lateBooking.getBookingStatus());
        if (!status.isConfirmed()) {
            throw new InvalidDataException("Đặt bàn muộn đến yêu cầu trạng thái phải là ĐÃ_XÁC_NHẬN (CONFIRMED)");
        }

        if (lateBooking.getCheckInAt() != null) {
            throw new InvalidDataException("Đặt bàn muộn đến yêu cầu chưa có check-in trên đặt bàn gốc");
        }

        Instant expectedArrive = lateBooking.getExpectedArriveTime();
        if (expectedArrive == null || !now.isAfter(expectedArrive.plus(Duration.ofMinutes(30)))) {
            throw new InvalidDataException("Đặt bàn muộn đến yêu cầu quá ngưỡng no-show trên 30 phút");
        }
    }

    private TableBooking loadActiveBooking(Integer bookingId) {
        return tableBookingRepository.findByIdAndActiveTrue(bookingId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đặt bàn với id: " + bookingId));
    }

    private Integer requireTableId(TableBooking booking) {
        if (booking.getTable() == null || booking.getTable().getId() == null) {
            throw new InvalidDataException("Không tìm thấy thông tin bàn liên kết với đặt bàn này");
        }
        return booking.getTable().getId();
    }

    private Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new InvalidDataException(message);
        }
        return value;
    }
}
