package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableExportDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableAvailableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.TableMapper;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlTableRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.service.TableService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableServiceImpl implements TableService {
    private final NativeSqlTableRepository nativeSqlTableRepository;
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final ServiceSupport serviceSupport;

    @Override
    public List<TableSearchResponseDTO> list(TableListRequestDTO request) {
        TableListRequestDTO safeRequest = request != null ? request : new TableListRequestDTO();

        List<TableEntity> tables;
        if (safeRequest.getActive() == null || safeRequest.getActive()) {
            tables = tableRepository.findAllByActiveTrueOrderByFloorAscSlotAscIdAsc();
        } else {
            tables = tableRepository.findAll(Sort.by(Sort.Direction.ASC, "floor", "slot", "id"));
        }

        return tables.stream()
                .map(tableMapper::toSearchResponseDTO)
                .toList();
    }

    @Override
    /**
     * Tìm kiếm danh sách bàn theo tiêu chí lọc và phân trang.
     */
    public PageResponse<List<TableSearchResponseDTO>> search(TableSearchRequestDTO request) {
        /**
         * Flow:
         * 1. Gọi native repository để lấy aggregate view của bàn
         * 2. Map sang DTO hiển thị
         * 3. Giữ nguyên metadata page
         */
        PageResponse<List<TableSearchNativeResultDTO>> pageResult = nativeSqlTableRepository.search(request);

        List<TableSearchResponseDTO> mapped = pageResult.getRows().stream()
                .map(tableMapper::toSearchResponseDTO)
                .toList();

        PageResponse<List<TableSearchResponseDTO>> response = new PageResponse<>();
        response.setRows(mapped);
        response.setPageNo(pageResult.getPageNo());
        response.setPageSize(pageResult.getPageSize());
        response.setTotalElements(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        return response;
    }

        @Override
        public List<TableExportDTO> exportData(TableSearchRequestDTO request) {
        List<TableSearchNativeResultDTO> rows = nativeSqlTableRepository.searchAll(request);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        return rows.stream()
            .map(row -> TableExportDTO.builder()
                .tableId(row.getTableId())
                .tableCode(row.getTableCode())
                .tableName(row.getTableName())
                .tableStatus(row.getTableStatus())
                .tableStatusName(TableStatusEnum.fromCode(row.getTableStatus()).getLabel())
                .floor(row.getFloor())
                .slot(row.getSlot())
                .totalBooking(row.getTotalBooking())
                .lastBookingTime(row.getLastBookingTime() != null ? formatter.format(row.getLastBookingTime()) : null)
                .active(Boolean.TRUE.equals(row.getActive()) ? "Hoạt động" : "Không hoạt động")
                .build())
            .collect(Collectors.toList());
        }

    @Override
    /**
     * Lấy danh sách bàn có trạng thái AVAILABLE.
     */
    public List<TableAvailableResponseDTO> availableTables(TableAvailableSearchRequestDTO request) {
        List<TableAvailableNativeResultDTO> availableTables = nativeSqlTableRepository.findAvailableTables(request);
        return availableTables.stream()
                .map(tableMapper::toAvailableResponseDTO)
                .toList();
    }

    @Override
    /**
     * Lấy chi tiết thông tin của một bàn theo id.
     */
    public TableDetailResponseDTO detail(TableDetailRequestDTO request) {
        /**
         * Flow:
         * 1. Lấy bàn active theo id
         * 2. Không có thì throw InvalidDataException
         * 3. Map detail response
         */
        TableEntity table = tableRepository.findByIdAndActiveTrue(request.getTableId())
                .orElseThrow(() -> new InvalidDataException(
                        "Table not found with id: " + request.getTableId()
                ));

        return tableMapper.toDetailResponseDTO(table);
    }

    @Override
    @Transactional
    /**
     * Cập nhật thủ công trạng thái của bàn.
     */
    public Boolean updateStatus(TableStatusUpdateRequestDTO request) {
        /**
         * Flow update table status:
         * 1. Validate mã trạng thái đầu vào
         * 2. Load bàn đang active
         * 3. Set trạng thái mới cho bàn
         * 4. Save thay đổi và trả kết quả
         */
        serviceSupport.validateTableStatusCode(request.getTableStatus());

        TableEntity table = serviceSupport.getActiveTable(request.getTableId());
        table.setTableStatus(TableStatusEnum.fromCode(request.getTableStatus()).getCode());
        tableRepository.save(table);
        return true;
    }

    @Override
    @Transactional
    public TableDetailResponseDTO create(TableCreateRequestDTO request) {
        TableEntity table = new TableEntity();
        table.setTableCode(request.getTableCode());
        table.setTableName(request.getTableName());
        table.setFloor(request.getFloor());
        table.setSlot(request.getSlot());
        table.setCreatedTime(Instant.now());
        table.setActive(true);

        if (StringUtils.hasText(request.getTableStatus())) {
            serviceSupport.validateTableStatusCode(request.getTableStatus());
            table.setTableStatus(TableStatusEnum.fromCode(request.getTableStatus()).getCode());
        } else {
            table.setTableStatus(TableStatusEnum.AVAILABLE.getCode());
        }

        TableEntity saved = tableRepository.save(table);
        return tableMapper.toDetailResponseDTO(saved);
    }

    @Override
    @Transactional
    public TableDetailResponseDTO update(TableUpdateRequestDTO request) {
        TableEntity table = serviceSupport.getActiveTable(request.getId());

        if (request.getTableCode() != null) {
            table.setTableCode(request.getTableCode());
        }
        if (request.getTableName() != null && !request.getTableName().isBlank()) {
            table.setTableName(request.getTableName());
        }
        if (request.getFloor() != null) {
            table.setFloor(request.getFloor());
        }
        if (request.getSlot() != null) {
            table.setSlot(request.getSlot());
        }
        if (StringUtils.hasText(request.getTableStatus())) {
            serviceSupport.validateTableStatusCode(request.getTableStatus());
            table.setTableStatus(TableStatusEnum.fromCode(request.getTableStatus()).getCode());
        }
        if (request.getActive() != null) {
            table.setActive(request.getActive());
        }

        TableEntity saved = tableRepository.save(table);
        return tableMapper.toDetailResponseDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        TableEntity table = serviceSupport.getActiveTable(id);
        table.setActive(false);
        tableRepository.save(table);
    }
}
