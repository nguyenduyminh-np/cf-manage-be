package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckInRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckOutRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDepositRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingExtendRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.event.booking.BookingDomainEventPublisher;
import com.duyminhdev.cf_manager.event.booking.BookingMutationType;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.mapper.TableBookingMapper;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.spec.TableBookingSpec;
import com.duyminhdev.cf_manager.service.booking.BookingUseCaseService;
import com.duyminhdev.cf_manager.service.TableBookingService;
import com.duyminhdev.cf_manager.state_machine.booking.BookingStateMachine;
import com.duyminhdev.cf_manager.state_machine.booking.BookingTransitionContext;
import com.duyminhdev.cf_manager.utils.PageUtils;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableBookingServiceImpl implements TableBookingService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "expectedArriveTime",
            "checkInAt",
            "expectedCheckOut",
            "checkOutAt",
            "bookingStatus",
            "customerName",
            "phoneNumber",
            "depositAmount",
            "createdAt"
    );

    private final TableBookingRepository tableBookingRepository;
    private final TableBookingMapper tableBookingMapper;
    private final ServiceSupport serviceSupport;
    private final BookingStateMachine bookingStateMachine;
    private final BookingLockService bookingLockService;
    private final BookingDomainEventPublisher bookingDomainEventPublisher;
    private final BookingUseCaseService bookingUseCaseService;

    @Override
    public PageResponse<List<TableBookingResponseDTO>> search(TableBookingSearchRequestDTO request) {
        TableBookingSearchRequestDTO safeRequest = normalizeSearchRequest(request);

        int pageNo = PageUtils.normalizePage(safeRequest.getPage());
        int pageSize = PageUtils.normalizeLimit(safeRequest.getLimit());

        String resolvedSortField = resolveSortFieldOrDefault(safeRequest.getSortField());
        String resolvedSortDir = resolveSortDirOrDefault(safeRequest.getSortDir());

        Pageable pageable = PageRequest.of(
                pageNo,
                pageSize,
                TableBookingSpec.resolveSort(resolvedSortField, resolvedSortDir)
        );

        Page<TableBooking> page = tableBookingRepository.findAll(
                TableBookingSpec.byCriteria(safeRequest),
                pageable
        );

        List<TableBookingResponseDTO> data = page.getContent().stream()
                .map(tableBookingMapper::toResponseDTO)
                .toList();

        PageResponse<List<TableBookingResponseDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    private TableBookingSearchRequestDTO normalizeSearchRequest(TableBookingSearchRequestDTO request) {
        TableBookingSearchRequestDTO safeRequest =
                request != null ? request : new TableBookingSearchRequestDTO();

        safeRequest.setBookingStatus(trimToNull(safeRequest.getBookingStatus()));
        safeRequest.setCustomerName(trimToNull(safeRequest.getCustomerName()));
        safeRequest.setPhoneNumber(trimToNull(safeRequest.getPhoneNumber()));
        safeRequest.setSortField(trimToNull(safeRequest.getSortField()));
        safeRequest.setSortDir(trimToNull(safeRequest.getSortDir()));

        return safeRequest;
    }

    private String resolveSortFieldOrDefault(String sortField) {
        String normalized = trimToNull(sortField);
        if (normalized == null) {
            return "expectedArriveTime";
        }
        return ALLOWED_SORT_FIELDS.contains(normalized) ? normalized : "expectedArriveTime";
    }

    private String resolveSortDirOrDefault(String sortDir) {
        return "ASC".equalsIgnoreCase(trimToNull(sortDir)) ? "ASC" : "DESC";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @Override
    @Transactional
    public TableBookingResponseDTO create(TableBookingCreateRequestDTO request) {
        TableBooking saved = bookingUseCaseService.createBooking(request);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO update(TableBookingUpdateRequestDTO request) {
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
        BookingStatusEnum targetStatus = BookingStatusEnum.fromCode(bookingStatus);
        serviceSupport.validateBookingStatusCode(targetStatus.getCode());

        TableBooking beforeLock = tableBookingRepository.findByIdAndActiveTrue(request.getBookingId())
                .orElseThrow(() -> new InvalidDataException(
                        "Booking not found with id: " + request.getBookingId()
                ));

        Integer oldTableId = beforeLock.getTable() != null ? beforeLock.getTable().getId() : null;
        Integer newTableId = request.getTableId();
        List<Integer> tableIdsToLock = new ArrayList<>();
        tableIdsToLock.add(oldTableId);
        tableIdsToLock.add(newTableId);

        return bookingLockService.executeWithTableLocks(tableIdsToLock, () -> {
            TableBooking existing = tableBookingRepository.findByIdAndActiveTrue(request.getBookingId())
                    .orElseThrow(() -> new InvalidDataException(
                            "Booking not found with id: " + request.getBookingId()
                    ));

            Integer currentOldTableId = existing.getTable() != null ? existing.getTable().getId() : null;
            TableEntity newTable = serviceSupport.getActiveTable(request.getTableId());

            tableBookingMapper.updateEntityFromRequest(request, existing);
            existing.setTable(newTable);

            bookingStateMachine.transition(BookingTransitionContext.builder()
                .booking(existing)
                .targetStatus(targetStatus)
                .allowNoopTransition(true)
                .build());

            TableBooking saved = tableBookingRepository.save(existing);

            if (currentOldTableId != null) {
                serviceSupport.recomputeAndSyncTableStatus(currentOldTableId);
            }
            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.UPDATE, saved.getId(), saved.getTable().getId());

            return tableBookingMapper.toResponseDTO(saved);
        });
    }

    @Override
    @Transactional
    public Boolean updateStatus(TableBookingStatusUpdateRequestDTO request) {
        serviceSupport.validateBookingStatusCode(request.getBookingStatus());

        BookingStatusEnum targetStatus = BookingStatusEnum.fromCode(request.getBookingStatus());

        switch (targetStatus) {
            case CONFIRMED -> bookingUseCaseService.confirmBooking(request.getBookingId());
            case CHECKED_IN -> bookingUseCaseService.checkIn(request.getBookingId(), request.getCheckInAt(), false);
            case COMPLETED -> bookingUseCaseService.checkOut(request.getBookingId(), request.getCheckOutAt());
            case CANCELLED -> bookingUseCaseService.cancelBooking(request.getBookingId());
            case EXPIRED -> bookingUseCaseService.expireBooking(request.getBookingId());
            case PENDING -> throw new InvalidDataException("updateStatus does not support transition to PENDING_CONFIRMATION");
        }

        return true;
    }

    @Override
    @Transactional
    public TableBookingResponseDTO confirm(Integer bookingId) {
        TableBooking saved = bookingUseCaseService.confirmBooking(bookingId);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO checkIn(Integer bookingId, TableBookingCheckInRequestDTO request) {
        TableBookingCheckInRequestDTO safeRequest = request != null ? request : new TableBookingCheckInRequestDTO();
        boolean force = Boolean.TRUE.equals(safeRequest.getForce());
        TableBooking saved = bookingUseCaseService.checkIn(bookingId, safeRequest.getCheckInAt(), force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO checkOut(Integer bookingId, TableBookingCheckOutRequestDTO request) {
        TableBookingCheckOutRequestDTO safeRequest = request != null ? request : new TableBookingCheckOutRequestDTO();
        TableBooking saved = bookingUseCaseService.checkOut(bookingId, safeRequest.getCheckOutAt());
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO cancel(Integer bookingId) {
        TableBooking saved = bookingUseCaseService.cancelBooking(bookingId);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO extend(Integer bookingId, TableBookingExtendRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("extend request is required");
        }

        boolean force = Boolean.TRUE.equals(request.getForce());
        TableBooking saved = bookingUseCaseService.extendBooking(bookingId, request.getExpectedCheckOut(), force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO createWalkIn(TableBookingCreateRequestDTO request, boolean force) {
        TableBooking saved = bookingUseCaseService.createWalkIn(request, force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO deposit(Integer bookingId, TableBookingDepositRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("deposit request is required");
        }

        TableBooking saved = bookingUseCaseService.markDepositPaid(
                bookingId,
                request.getDepositAmount(),
                request.getDepositTxnRef(),
                request.getDepositPaidAt()
        );
        return tableBookingMapper.toResponseDTO(saved);
    }
}