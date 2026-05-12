package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.base.ServiceResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingAvailableSlotsRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCancelRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckInRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckOutRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingConfirmRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDepositRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingExtendRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingWalkInRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingAvailableSlotResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingExportDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.event.booking.BookingDomainEventPublisher;
import com.duyminhdev.cf_manager.event.booking.BookingMutationType;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.lock.booking.BookingLockService;
import com.duyminhdev.cf_manager.mapper.TableBookingMapper;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlTableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.spec.TableBookingSpec;
import com.duyminhdev.cf_manager.service.booking.BookingUseCaseService;
import com.duyminhdev.cf_manager.service.booking.BookingNotificationService;
import com.duyminhdev.cf_manager.service.TableBookingService;
import com.duyminhdev.cf_manager.state_machine.booking.BookingStateMachine;
import com.duyminhdev.cf_manager.state_machine.booking.BookingTransitionContext;
import com.duyminhdev.cf_manager.utils.PageUtils;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final List<String> SLOT_BLOCKING_STATUSES = List.of(
            BookingStatusEnum.CONFIRMED.getCode(),
            BookingStatusEnum.CHECKED_IN.getCode()
    );

    private final TableBookingRepository tableBookingRepository;
    private final TableBookingMapper tableBookingMapper;
    private final ServiceSupport serviceSupport;
    private final BookingStateMachine bookingStateMachine;
    private final BookingLockService bookingLockService;
    private final BookingDomainEventPublisher bookingDomainEventPublisher;
    private final BookingUseCaseService bookingUseCaseService;
    private final NativeSqlTableBookingRepository nativeSqlTableBookingRepository;
    private final BookingNotificationService bookingNotificationService;

    @Override
    public List<TableBookingResponseDTO> getPendingAndConfirmedBookings(TableBookingSearchRequestDTO request) {
        Integer tableId = request != null ? request.getTableId() : null;
        List<TableBookingDetailNativeResultDTO> nativeResults =
                nativeSqlTableBookingRepository.findPendingAndConfirmedBookings(tableId);

        return nativeResults.stream()
                .map(tableBookingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

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

        @Override
        public List<TableBookingExportDTO> exportData(TableBookingSearchRequestDTO request) {
        TableBookingSearchRequestDTO safeRequest = normalizeSearchRequest(request);

        String resolvedSortField = resolveSortFieldOrDefault(safeRequest.getSortField());
        String resolvedSortDir = resolveSortDirOrDefault(safeRequest.getSortDir());

        List<TableBooking> bookings = tableBookingRepository.findAll(
            TableBookingSpec.byCriteria(safeRequest),
            TableBookingSpec.resolveSort(resolvedSortField, resolvedSortDir)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        return bookings.stream()
            .map(booking -> TableBookingExportDTO.builder()
                .bookingId(booking.getId())
                .bookingInvoiceCode(booking.getBookingInvoiceCode())
                .tableId(booking.getTable() != null ? booking.getTable().getId() : null)
                .tableCode(booking.getTable() != null ? booking.getTable().getTableCode() : null)
                .tableName(booking.getTable() != null ? booking.getTable().getTableName() : null)
                .expectedArriveTime(booking.getExpectedArriveTime() != null ? formatter.format(booking.getExpectedArriveTime()) : null)
                .checkInAt(booking.getCheckInAt() != null ? formatter.format(booking.getCheckInAt()) : null)
                .expectedCheckOut(booking.getExpectedCheckOut() != null ? formatter.format(booking.getExpectedCheckOut()) : null)
                .checkOutAt(booking.getCheckOutAt() != null ? formatter.format(booking.getCheckOutAt()) : null)
                .bookingStatus(booking.getBookingStatus())
                .bookingStatusName(resolveBookingStatusLabel(booking.getBookingStatus()))
                .customerName(booking.getCustomerName())
                .phoneNumber(booking.getPhoneNumber())
                .depositAmount(booking.getDepositAmount())
                .depositPaid(booking.getDepositPaid())
                .depositPaidAt(booking.getDepositPaidAt() != null ? formatter.format(booking.getDepositPaidAt()) : null)
                .depositForfeited(booking.getDepositForfeited())
                .depositTxnRef(booking.getDepositTxnRef())
                .note(booking.getNote())
                .accountId(booking.getAccount() != null ? booking.getAccount().getId() : null)
                .accountUsername(booking.getAccount() != null ? booking.getAccount().getUsername() : null)
                .accountFullName(booking.getAccount() != null ? booking.getAccount().getFullName() : null)
                .active(booking.getActive())
                .createdAt(booking.getCreatedAt() != null ? formatter.format(booking.getCreatedAt()) : null)
                .build())
            .collect(Collectors.toList());
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

    private String resolveBookingStatusLabel(String bookingStatus) {
        if (!StringUtils.hasText(bookingStatus)) {
            return null;
        }
        try {
            return BookingStatusEnum.fromCode(bookingStatus).getLabel();
        } catch (Exception ex) {
            return bookingStatus;
        }
    }

    @Override
    @Transactional
    public ServiceResult<TableBookingResponseDTO> create(TableBookingCreateRequestDTO request) {
        TableBooking saved;

        if (request != null && Boolean.TRUE.equals(request.getIsWalkIn())) {
            saved = bookingUseCaseService.createWalkIn(request, false);
            return ServiceResult.ok(tableBookingMapper.toResponseDTO(saved));
        }

        saved = bookingUseCaseService.createBooking(request);
        TableBookingResponseDTO dto = tableBookingMapper.toResponseDTO(saved);

        // --- Kiểm tra cảnh báo: bàn có đơn CONFIRMED sắp tới trong 2 tiếng ---
        List<String> warnings = buildUpcomingBookingWarnings(saved);

        if (!warnings.isEmpty()) {
            // Publish WebSocket warning notification (kênh riêng, không làm toàn hệ thống bất ngờ)
            bookingNotificationService.sendOnce(
                    BookingSchedulerConstant.TOPIC_TABLE_ALERTS,
            BookingSchedulerConstant.DEDUP_KEY_UPCOMING_BOOKING_WARN_PREFIX
                            + saved.getId() + ":" + saved.getTable().getId(),
                    buildUpcomingWarningWsPayload(saved, warnings)
            );
        }

        return warnings.isEmpty()
                ? ServiceResult.ok(dto)
                : ServiceResult.withWarnings(dto, warnings);
    }

    /**
     * Kiểm tra xem sau khi tạo xong, bàn có đơn CONFIRMED nào sắp tới trong 2 tiếng không.
     * Nếu có: thêm note vào đơn vừa tạo và trả về danh sách warning message.
     */
    private List<String> buildUpcomingBookingWarnings(TableBooking saved) {
        Instant now = Instant.now();
        Instant twoHoursLater = now.plus(Duration.ofHours(BookingSchedulerConstant.UPCOMING_BOOKING_WARN_HOURS));

        boolean hasUpcoming = tableBookingRepository.existsUpcomingConfirmedBookingInWindow(
                saved.getTable().getId(),
                now,
                twoHoursLater,
                saved.getId()   // loại trừ chính đơn vừa tạo
        );

        if (!hasUpcoming) {
            return List.of();
        }

        // Append vào note của đơn
        String warningNote = "⚠️ Bàn đã có đơn đặt xác nhận sắp tới trong vòng 2 tiếng";
        String currentNote = saved.getNote();
        saved.setNote(currentNote != null && !currentNote.isBlank()
                ? currentNote + " | " + warningNote
                : warningNote);
        tableBookingRepository.save(saved);

        String warningMsg = "Bàn " + saved.getTable().getTableCode()
                + " đã có đơn đặt bàn được xác nhận sắp tới trong vòng 2 tiếng tới. Vui lòng kiểm tra lại lịch trước khi xác nhận đơn này.";
        return List.of(warningMsg);
    }

    private java.util.Map<String, Object> buildUpcomingWarningWsPayload(TableBooking saved, List<String> warnings) {
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("event", "BOOKING_UPCOMING_CONFLICT_WARNING");
        payload.put("bookingId", saved.getId());
        payload.put("tableId", saved.getTable().getId());
        payload.put("tableCode", saved.getTable().getTableCode());
        payload.put("severity", "WARNING");
        payload.put("message", warnings.get(0));
        payload.put("at", Instant.now().toString());
        return payload;
    }

    @Override
    public TableBookingResponseDTO detail(TableBookingDetailRequestDTO request) {
        TableBookingDetailNativeResultDTO row =
                nativeSqlTableBookingRepository.findBookingDetailByBookingId(request.getBookingId());

        if (row == null) {
            throw new InvalidDataException(
                    "Không tìm thấy thấy chi tiết đặt bàn với bookingId: " + request.getBookingId()
            );
        }

        return tableBookingMapper.toResponseDTO(row);
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
                            "Không tìm thấy đặt bàn với id: " + request.getBookingId()
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
            case PENDING -> throw new InvalidDataException("updateStatus không hỗ trợ chuyển trạng thái về CHờ_XÁC_NHẬN");
        }

        return true;
    }

    @Override
    @Transactional
    public TableBookingResponseDTO confirm(TableBookingConfirmRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu xác nhận booking không được để trống");
        }

        TableBooking saved = bookingUseCaseService.confirmBooking(request.getBookingId());
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO checkIn(TableBookingCheckInRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu check-in không được để trống");
        }

        boolean force = Boolean.TRUE.equals(request.getForce());
        TableBooking saved = bookingUseCaseService.checkIn(request.getBookingId(), request.getCheckInAt(), force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO checkOut(TableBookingCheckOutRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu check-out không được để trống");
        }

        TableBooking saved = bookingUseCaseService.checkOut(request.getBookingId(), request.getCheckOutAt());
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO cancel(TableBookingCancelRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu hủy booking không được để trống");
        }

        TableBooking saved = bookingUseCaseService.cancelBooking(request.getBookingId());
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO extend(TableBookingExtendRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu gia hạn (extend request) không được để trống");
        }

        boolean force = Boolean.TRUE.equals(request.getForce());
        TableBooking saved = bookingUseCaseService.extendBooking(request.getBookingId(), request.getExpectedCheckOut(), force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO createWalkIn(TableBookingWalkInRequestDTO request, boolean force) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu walk-in không được để trống");
        }
        if (request.getTableId() == null || request.getTableId() <= 0) {
            throw new InvalidDataException("Mã bàn (tableId) là bắt buộc");
        }

        TableBooking saved = bookingUseCaseService.createWalkIn(toCreateRequest(request), force);
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO createWalkInFromLateArrival(
            Integer lateBookingId,
            TableBookingWalkInRequestDTO request,
            boolean force
    ) {
        TableBooking saved = bookingUseCaseService.createWalkInFromLateArrival(
                lateBookingId,
                toCreateRequestOrNull(request),
                force
        );
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableBookingResponseDTO deposit(TableBookingDepositRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu đặt cọc (deposit request) không được để trống");
        }

        TableBooking saved = bookingUseCaseService.markDepositPaid(
                request.getBookingId(),
                request.getDepositAmount(),
                request.getDepositTxnRef(),
                request.getDepositPaidAt()
        );
        return tableBookingMapper.toResponseDTO(saved);
    }

    @Override
    public List<TableBookingAvailableSlotResponseDTO> getAvailableSlots(TableBookingAvailableSlotsRequestDTO request) {
        if (request == null) {
            throw new InvalidDataException("Yêu cầu tra cứu khung giờ trống không được để trống");
        }

        Integer tableId = request.getTableId();
        LocalDate date = request.getDate();

        if (tableId == null || tableId <= 0) {
            throw new InvalidDataException("Mã bàn (tableId) là bắt buộc");
        }
        if (date == null) {
            throw new InvalidDataException("Ngày tra cứu (date) là bắt buộc");
        }

        serviceSupport.getActiveTable(tableId);

        Instant dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant dayEnd = dayStart.plusSeconds(86_400L);

        List<TableBooking> blockingBookings = tableBookingRepository.findActiveBookingsOnTableBetweenByStatuses(
                tableId,
                dayStart,
                dayEnd,
                SLOT_BLOCKING_STATUSES
        );

        return buildAvailableSlots(dayStart, dayEnd, blockingBookings);
    }

    @Override
    @Transactional
    public void delete(Integer bookingId) {
        TableBooking booking = tableBookingRepository.findById(bookingId)
                .orElseThrow(() -> new InvalidDataException("Booking not found"));

        Integer tableId = booking.getTable().getId();
        tableBookingRepository.deleteById(bookingId);
        serviceSupport.recomputeAndSyncTableStatus(tableId);
    }

    private List<TableBookingAvailableSlotResponseDTO> buildAvailableSlots(
            Instant dayStart,
            Instant dayEnd,
            List<TableBooking> bookings
    ) {
        List<TableBookingAvailableSlotResponseDTO> slots = new ArrayList<>();
        Instant cursor = dayStart;

        for (TableBooking booking : bookings) {
            if (booking.getExpectedArriveTime() == null || booking.getExpectedCheckOut() == null) {
                continue;
            }

                Instant occupiedStart = booking.getExpectedArriveTime().isBefore(dayStart)
                    ? dayStart
                    : booking.getExpectedArriveTime();
                Instant occupiedEnd = booking.getExpectedCheckOut().isAfter(dayEnd)
                    ? dayEnd
                    : booking.getExpectedCheckOut();

            if (occupiedStart.isAfter(cursor)) {
                slots.add(TableBookingAvailableSlotResponseDTO.builder()
                        .slotStart(cursor)
                        .slotEnd(occupiedStart)
                        .build());
            }

            if (occupiedEnd.isAfter(cursor)) {
                cursor = occupiedEnd;
            }

            if (!cursor.isBefore(dayEnd)) {
                break;
            }
        }

        if (cursor.isBefore(dayEnd)) {
            slots.add(TableBookingAvailableSlotResponseDTO.builder()
                    .slotStart(cursor)
                    .slotEnd(dayEnd)
                    .build());
        }

        return slots;
    }

    private TableBookingCreateRequestDTO toCreateRequest(TableBookingWalkInRequestDTO request) {
        TableBookingCreateRequestDTO converted = toCreateRequestOrNull(request);
        if (converted == null) {
            throw new InvalidDataException("Yêu cầu walk-in không được để trống");
        }
        return converted;
    }

    private TableBookingCreateRequestDTO toCreateRequestOrNull(TableBookingWalkInRequestDTO request) {
        if (request == null) {
            return null;
        }

        TableBookingCreateRequestDTO converted = new TableBookingCreateRequestDTO();
        converted.setTableId(request.getTableId());
        converted.setExpectedArriveTime(request.getExpectedArriveTime());
        converted.setExpectedCheckOut(request.getExpectedCheckOut());
        converted.setCustomerName(request.getCustomerName());
        converted.setPhoneNumber(request.getPhoneNumber());
        converted.setDepositAmount(request.getDepositAmount());
        converted.setDepositPaid(request.getDepositPaid());
        converted.setDepositPaidAt(request.getDepositPaidAt());
        converted.setDepositForfeited(request.getDepositForfeited());
        converted.setDepositTxnRef(request.getDepositTxnRef());
        converted.setBookingStatus(request.getBookingStatus());
        converted.setNote(request.getNote());
        return converted;
    }
}