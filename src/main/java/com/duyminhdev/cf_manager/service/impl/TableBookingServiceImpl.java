package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
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
        response.setData(data);
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
        return bookingLockService.executeWithTableLock(request.getTableId(), () -> {
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

            TableEntity table = serviceSupport.getActiveTable(request.getTableId());
            Account account = serviceSupport.getCurrentAccount();

            TableBooking entity = tableBookingMapper.toNewEntity(request);
            entity.setTable(table);
            entity.setAccount(account);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setActive(true);

            bookingStateMachine.initialize(entity, targetStatus);

            TableBooking saved = tableBookingRepository.save(entity);

            serviceSupport.recomputeAndSyncTableStatus(saved.getTable().getId());
            bookingDomainEventPublisher.publish(BookingMutationType.CREATE, saved.getId(), saved.getTable().getId());

            return tableBookingMapper.toResponseDTO(saved);
        });
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

        TableBooking beforeLock = tableBookingRepository.findByIdAndActiveTrue(request.getBookingId())
                .orElseThrow(() -> new InvalidDataException(
                        "Booking not found with id: " + request.getBookingId()
                ));

        Integer tableId = beforeLock.getTable() != null ? beforeLock.getTable().getId() : null;
        return bookingLockService.executeWithTableLock(tableId, () -> {
            TableBooking existing = tableBookingRepository.findByIdAndActiveTrue(request.getBookingId())
                    .orElseThrow(() -> new InvalidDataException(
                            "Booking not found with id: " + request.getBookingId()
                    ));

            BookingStatusEnum targetStatus = BookingStatusEnum.fromCode(request.getBookingStatus());
            bookingStateMachine.transition(BookingTransitionContext.builder()
                .booking(existing)
                .targetStatus(targetStatus)
                .requestedCheckInAt(request.getCheckInAt())
                .requestedCheckOutAt(request.getCheckOutAt())
                .allowNoopTransition(true)
                .build());

            tableBookingRepository.save(existing);

            serviceSupport.recomputeAndSyncTableStatus(existing.getTable().getId());
                bookingDomainEventPublisher.publish(
                    BookingMutationType.UPDATE_STATUS,
                    existing.getId(),
                    existing.getTable().getId()
                );

            return true;
        });
    }
}