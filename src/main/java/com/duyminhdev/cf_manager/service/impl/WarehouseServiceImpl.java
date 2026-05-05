package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.WarehouseSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.warehouse.*;
import com.duyminhdev.cf_manager.dto.response.warehouse.*;
import com.duyminhdev.cf_manager.entity.Warehouse;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlWarehouseRepository;
import com.duyminhdev.cf_manager.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final NativeSqlWarehouseRepository nativeSqlWarehouseRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Override
    public PageResponse<List<WarehouseResponseDTO>> search(WarehouseSearchRequestDTO request) {
        PageResponse<List<WarehouseSearchNativeResultDTO>> nativePage =
                nativeSqlWarehouseRepository.search(request);
        List<WarehouseResponseDTO> data = nativePage.getRows().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        PageResponse<List<WarehouseResponseDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(nativePage.getPageNo());
        response.setPageSize(nativePage.getPageSize());
        response.setTotalElements(nativePage.getTotalElements());
        response.setTotalPages(nativePage.getTotalPages());
        return response;
    }

    @Override
    public List<WarehouseExportDTO> exportData(WarehouseSearchRequestDTO request) {
        List<WarehouseSearchNativeResultDTO> all = nativeSqlWarehouseRepository.findAllByCriteria(request);
        return all.stream().map(dto -> {
            WarehouseExportDTO export = new WarehouseExportDTO();
            export.setWarehouseCode(dto.getWarehouseCode());
            export.setWarehouseName(dto.getWarehouseName());
            export.setLocation(dto.getLocation());
            export.setNote(dto.getNote());
            export.setActive(Boolean.TRUE.equals(dto.getActive()) ? "Hoạt động" : "Không hoạt động");
            return export;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WarehouseDetailResponseDTO create(WarehouseCreateRequestDTO request) {
        // Kiểm tra trùng mã kho nếu có
        if (StringUtils.hasText(request.getWarehouseCode())) {
            warehouseRepository.findByWarehouseCode(request.getWarehouseCode()).ifPresent(w -> {
                throw new InvalidDataException("Mã kho '" + request.getWarehouseCode() + "' đã tồn tại");
            });
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(request.getWarehouseCode());
        warehouse.setWarehouseName(request.getWarehouseName());
        warehouse.setLocation(request.getLocation());
        warehouse.setNote(request.getNote());
        warehouse.setCreatedTime(Instant.now());
        warehouse.setActive(true);

        warehouse = warehouseRepository.save(warehouse);
        return mapToDetail(warehouse);
    }

    @Override
    @Transactional
    public WarehouseDetailResponseDTO update(WarehouseUpdateRequestDTO request) {
        Warehouse warehouse = warehouseRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Kho không tồn tại"));

        // Xử lý cập nhật mã kho (kiểm tra trùng nếu có thay đổi)
        if (StringUtils.hasText(request.getWarehouseCode())
                && !request.getWarehouseCode().equals(warehouse.getWarehouseCode())) {
            warehouseRepository.findByWarehouseCode(request.getWarehouseCode()).ifPresent(w -> {
                throw new InvalidDataException("Mã kho '" + request.getWarehouseCode() + "' đã tồn tại");
            });
            warehouse.setWarehouseCode(request.getWarehouseCode());
        }

        if (StringUtils.hasText(request.getWarehouseName())) {
            warehouse.setWarehouseName(request.getWarehouseName());
        }
        if (request.getLocation() != null) {
            warehouse.setLocation(request.getLocation());
        }
        if (request.getNote() != null) {
            warehouse.setNote(request.getNote());
        }

        // Xử lý thay đổi trạng thái active (vô hiệu hóa phải kiểm tra phụ thuộc)
        if (request.getActive() != null) {
            if (!request.getActive() && Boolean.TRUE.equals(warehouse.getActive())) {
                // Đang cố vô hiệu hóa
                validateBeforeDeactivate(warehouse.getId());
            }
            warehouse.setActive(request.getActive());
        }

        warehouseRepository.save(warehouse);
        return mapToDetail(warehouse);
    }

    @Override
    @Transactional
    public void delete(WarehouseIdRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Kho không tồn tại"));

        if (Boolean.FALSE.equals(warehouse.getActive())) {
            throw new InvalidDataException("Kho đã bị vô hiệu hóa trước đó");
        }

        validateBeforeDeactivate(warehouse.getId());
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    @Override
    public WarehouseDetailResponseDTO getDetail(WarehouseIdRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Kho không tồn tại"));
        return mapToDetail(warehouse);
    }

    @Override
    public List<WarehouseOptionDTO> getOptions() {
        List<Warehouse> activeWarehouses = warehouseRepository.findAllByActiveTrueOrderByWarehouseNameAsc();
        return activeWarehouses.stream()
                .map(w -> WarehouseOptionDTO.builder()
                        .id(w.getId())
                        .warehouseCode(w.getWarehouseCode())
                        .warehouseName(w.getWarehouseName())
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------- private helpers --------------------

    private void validateBeforeDeactivate(Integer warehouseId) {
        long stockQty = stockLevelRepository.sumQuantityByWarehouse(warehouseId);
        if (stockQty > 0) {
            throw new InvalidDataException(
                "Không thể vô hiệu hóa kho vì còn " + stockQty +
                " đơn vị nguyên liệu trong tồn kho. Vui lòng xử lý xuất/chuyển hết.");
        }

        long pendingTx = stockTransactionRepository.countPendingTransactions(warehouseId);
        if (pendingTx > 0) {
            throw new InvalidDataException(
                "Không thể vô hiệu hóa kho vì có " + pendingTx +
                " giao dịch chưa hoàn tất. Vui lòng hoàn tất hoặc hủy trước.");
        }
    }

    private WarehouseDetailResponseDTO mapToDetail(Warehouse w) {
        return WarehouseDetailResponseDTO.builder()
                .id(w.getId())
                .warehouseCode(w.getWarehouseCode())
                .warehouseName(w.getWarehouseName())
                .location(w.getLocation())
                .note(w.getNote())
                .createdTime(w.getCreatedTime())
                .active(w.getActive())
                .build();
    }

    private WarehouseResponseDTO toResponseDTO(WarehouseSearchNativeResultDTO dto) {
        return WarehouseResponseDTO.builder()
                .id(dto.getId())
                .warehouseCode(dto.getWarehouseCode())
                .warehouseName(dto.getWarehouseName())
                .location(dto.getLocation())
                .note(dto.getNote())
                .createdTime(dto.getCreatedTime())
                .active(dto.getActive())
                .ingredientCount(dto.getIngredientCount())
                .build();
    }
}
