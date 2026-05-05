// IngredientServiceImpl.java
package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientIdRequest;
import com.duyminhdev.cf_manager.dto.request.ingredient.*;
import com.duyminhdev.cf_manager.dto.response.ingredient.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlIngredientRepository;
import com.duyminhdev.cf_manager.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientServiceImpl implements IngredientService {

    private final NativeSqlIngredientRepository nativeSqlIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryRepository ingredientCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;
    private final StockLevelRepository stockLevelRepository; // để lấy chi tiết lô

    private static final DateTimeFormatter EXCEL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public PageResponse<List<IngredientResponseDTO>> search(IngredientSearchRequestDTO request) {
        PageResponse<List<IngredientSearchNativeResultDTO>> nativePage =
                nativeSqlIngredientRepository.search(request);
        List<IngredientResponseDTO> data = nativePage.getRows().stream()
                .map(this::toResponseDTO)
                .toList();

        PageResponse<List<IngredientResponseDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(nativePage.getPageNo());
        response.setPageSize(nativePage.getPageSize());
        response.setTotalElements(nativePage.getTotalElements());
        response.setTotalPages(nativePage.getTotalPages());
        return response;
    }

    @Override
    public List<IngredientExportDTO> exportData(IngredientSearchRequestDTO request) {
        List<IngredientSearchNativeResultDTO> all = nativeSqlIngredientRepository.findAllByCriteria(request);
                return all.stream().map(this::toExportDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IngredientDetailResponseDTO create(IngredientCreateRequestDTO request) {
                if (StringUtils.hasText(request.getIngredientCode())) {
                        ingredientRepository.findByIngredientCode(request.getIngredientCode().trim()).ifPresent(existing -> {
                                throw new InvalidDataException("Mã nguyên liệu '" + request.getIngredientCode() + "' đã tồn tại");
                        });
                }

        Ingredient ingredient = new Ingredient();
                ingredient.setIngredientCode(StringUtils.hasText(request.getIngredientCode())
                                ? request.getIngredientCode().trim() : null);
        ingredient.setIngredientName(request.getIngredientName());
        ingredient.setSelfLife(request.getSelfLife());
                ingredient.setAveragePrice(null);
        ingredient.setCreatedTime(Instant.now());
        ingredient.setActive(true);

                ingredient.setIngredientCategory(getCategory(request.getIngredientCategoryId()));
                ingredient.setSupplier(getSupplier(request.getSupplierId()));
                ingredient.setUnit(getUnit(request.getUnitId()));

        ingredient = ingredientRepository.save(ingredient);
                return buildDetail(ingredient.getId());
    }

    @Override
    @Transactional
    public IngredientDetailResponseDTO update(IngredientUpdateRequestDTO request) {
        Ingredient ingredient = ingredientRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Nguyên liệu không tồn tại"));

                if (StringUtils.hasText(request.getIngredientCode())) {
                        String newCode = request.getIngredientCode().trim();
                        if (!newCode.equals(ingredient.getIngredientCode())) {
                                ingredientRepository.findByIngredientCode(newCode).ifPresent(existing -> {
                                        throw new InvalidDataException("Mã nguyên liệu '" + newCode + "' đã tồn tại");
                                });
                                ingredient.setIngredientCode(newCode);
                        }
        }
        if (StringUtils.hasText(request.getIngredientName())) {
            ingredient.setIngredientName(request.getIngredientName());
        }
        if (request.getSelfLife() != null) {
            ingredient.setSelfLife(request.getSelfLife());
        }
        if (request.getIngredientCategoryId() != null) {
                        ingredient.setIngredientCategory(getCategory(request.getIngredientCategoryId()));
        }
        if (request.getSupplierId() != null) {
                        ingredient.setSupplier(getSupplier(request.getSupplierId()));
        }
        if (request.getUnitId() != null) {
                        ingredient.setUnit(getUnit(request.getUnitId()));
        }
        if (request.getActive() != null) {
                        if (!request.getActive()) {
                                long availableQty = stockLevelRepository.sumAvailableQuantityByIngredientId(ingredient.getId());
                                if (availableQty > 0) {
                                        throw new InvalidDataException(
                                                        "Không thể vô hiệu hóa nguyên liệu vì vẫn còn " + availableQty +
                                                                        " tồn kho khả dụng. Vui lòng xử lý tồn kho trước.");
                                }
                        }
            ingredient.setActive(request.getActive());
        }

        ingredientRepository.save(ingredient);
                return buildDetail(ingredient.getId());
    }

    @Override
    @Transactional
        public void delete(IngredientIdRequest request) {
                Ingredient ingredient = ingredientRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Nguyên liệu không tồn tại"));

                long availableQty = stockLevelRepository.sumAvailableQuantityByIngredientId(ingredient.getId());
                if (availableQty > 0) {
                        throw new InvalidDataException(
                                        "Không thể xóa nguyên liệu vì vẫn còn " + availableQty +
                                                        " tồn kho khả dụng. Vui lòng xử lý tồn kho trước.");
                }
        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
    }

    @Override
        public IngredientDetailResponseDTO getDetail(IngredientIdRequest request) {
                return buildDetail(request.getId());
        }

        // ------------------- Private helper methods -------------------

        private IngredientDetailResponseDTO buildDetail(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Nguyên liệu không tồn tại"));

        List<StockLevel> stockLevels = stockLevelRepository
                .findByIngredientIdAndActiveTrueAndExpirationDateAfterOrderByExpirationDate(id, Instant.now());

        List<StockLevelDTO> stockDTOs = stockLevels.stream()
                .map(sl -> StockLevelDTO.builder()
                        .id(sl.getId())
                        .warehouseName(sl.getWarehouse().getWarehouseName())
                        .quantity(sl.getQuantity())
                        .expirationDate(sl.getExpirationDate())
                        .unitPrice(sl.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return IngredientDetailResponseDTO.builder()
                .id(ingredient.getId())
                .ingredientCode(ingredient.getIngredientCode())
                .ingredientName(ingredient.getIngredientName())
                .selfLife(ingredient.getSelfLife())
                .averagePrice(ingredient.getAveragePrice())
                .createdTime(ingredient.getCreatedTime())
                .active(ingredient.getActive())
                .ingredientCategoryId(ingredient.getIngredientCategory().getId())
                .ingredientCategoryName(ingredient.getIngredientCategory().getIngredientCategoryName())
                .supplierId(ingredient.getSupplier().getId())
                .supplierName(ingredient.getSupplier().getSupplierName())
                .unitId(ingredient.getUnit().getId())
                .unitName(ingredient.getUnit().getUnitName())
                .stockLevels(stockDTOs)
                .build();
    }

        private IngredientExportDTO toExportDTO(IngredientSearchNativeResultDTO dto) {
                IngredientExportDTO export = new IngredientExportDTO();
                export.setIngredientCode(dto.getIngredientCode());
                export.setIngredientName(dto.getIngredientName());
                export.setSelfLife(dto.getSelfLife());
                export.setAveragePrice(dto.getAveragePrice() != null ? dto.getAveragePrice().toString() : "Chưa có");
                export.setIngredientCategoryName(dto.getIngredientCategoryName());
                export.setSupplierName(dto.getSupplierName());
                export.setUnitName(dto.getUnitName());
                export.setActive(Boolean.TRUE.equals(dto.getActive()) ? "Hoạt động" : "Không hoạt động");
                return export;
        }

        private IngredientCategory getCategory(Integer id) {
                return ingredientCategoryRepository.findById(id)
                                .orElseThrow(() -> new InvalidDataException("Danh mục nguyên liệu không tồn tại"));
        }

        private Supplier getSupplier(Integer id) {
                return supplierRepository.findById(id)
                                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
        }

        private Unit getUnit(Integer id) {
                return unitRepository.findById(id)
                                .orElseThrow(() -> new InvalidDataException("Đơn vị tính không tồn tại"));
        }

    private IngredientResponseDTO toResponseDTO(IngredientSearchNativeResultDTO dto) {
        return IngredientResponseDTO.builder()
                .id(dto.getId())
                .ingredientCode(dto.getIngredientCode())
                .ingredientName(dto.getIngredientName())
                .selfLife(dto.getSelfLife())
                .averagePrice(dto.getAveragePrice())
                .createdTime(dto.getCreatedTime())
                .active(dto.getActive())
                .ingredientCategoryId(dto.getIngredientCategoryId())
                .ingredientCategoryName(dto.getIngredientCategoryName())
                .supplierId(dto.getSupplierId())
                .supplierName(dto.getSupplierName())
                .unitId(dto.getUnitId())
                .unitName(dto.getUnitName())
                .currentStock(dto.getCurrentStock())
                .build();
    }
}