package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientCategorySearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient_category.*;
import com.duyminhdev.cf_manager.dto.response.ingredient_category.*;
import com.duyminhdev.cf_manager.entity.IngredientCategory;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlIngredientCategoryRepository;
import com.duyminhdev.cf_manager.service.IngredientCategoryService;
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
public class IngredientCategoryServiceImpl implements IngredientCategoryService {

    private final NativeSqlIngredientCategoryRepository nativeSqlRepo;
    private final IngredientCategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public PageResponse<List<IngredientCategoryResponseDTO>> search(IngredientCategorySearchRequestDTO request) {
        PageResponse<List<IngredientCategorySearchNativeResultDTO>> nativePage = nativeSqlRepo.search(request);
        List<IngredientCategoryResponseDTO> data = nativePage.getRows().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        PageResponse<List<IngredientCategoryResponseDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(nativePage.getPageNo());
        response.setPageSize(nativePage.getPageSize());
        response.setTotalElements(nativePage.getTotalElements());
        response.setTotalPages(nativePage.getTotalPages());
        return response;
    }

    @Override
    public List<IngredientCategoryExportDTO> exportData(IngredientCategorySearchRequestDTO request) {
        List<IngredientCategorySearchNativeResultDTO> all = nativeSqlRepo.findAllByCriteria(request);
        return all.stream().map(dto -> {
            IngredientCategoryExportDTO export = new IngredientCategoryExportDTO();
            export.setIngredientCategoryCode(dto.getIngredientCategoryCode());
            export.setIngredientCategoryName(dto.getIngredientCategoryName());
            export.setParentCategoryName(dto.getParentCategoryName());
            export.setActive(Boolean.TRUE.equals(dto.getActive()) ? "Hoạt động" : "Không hoạt động");
            return export;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IngredientCategoryDetailResponseDTO create(IngredientCategoryCreateRequestDTO request) {
        // Kiểm tra trùng mã nếu có
        if (StringUtils.hasText(request.getIngredientCategoryCode())) {
            categoryRepository.findByIngredientCategoryCode(request.getIngredientCategoryCode())
                    .ifPresent(c -> { throw new InvalidDataException("Mã danh mục đã tồn tại"); });
        }

        IngredientCategory category = new IngredientCategory();
        category.setIngredientCategoryCode(request.getIngredientCategoryCode());
        category.setIngredientCategoryName(request.getIngredientCategoryName());
        category.setCreatedTime(Instant.now());
        category.setActive(true);

        // Gán danh mục cha nếu có
        if (request.getParentCategoryId() != null) {
            IngredientCategory parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new InvalidDataException("Danh mục cha không tồn tại"));
            // Không cho phép chọn chính nó (sẽ kiểm tra sau khi lưu, nhưng ở create thì id null nên không sao)
            category.setParentCategory(parent);
        }

        category = categoryRepository.save(category);
        return mapToDetail(category);
    }

    @Override
    @Transactional
    public IngredientCategoryDetailResponseDTO update(IngredientCategoryUpdateRequestDTO request) {
        IngredientCategory category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));

        // Cập nhật mã
        if (StringUtils.hasText(request.getIngredientCategoryCode())
                && !request.getIngredientCategoryCode().equals(category.getIngredientCategoryCode())) {
            categoryRepository.findByIngredientCategoryCode(request.getIngredientCategoryCode())
                    .ifPresent(c -> { throw new InvalidDataException("Mã danh mục đã tồn tại"); });
            category.setIngredientCategoryCode(request.getIngredientCategoryCode());
        }

        if (StringUtils.hasText(request.getIngredientCategoryName())) {
            category.setIngredientCategoryName(request.getIngredientCategoryName());
        }

        // Cập nhật danh mục cha
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(category.getId())) {
                throw new InvalidDataException("Không thể chọn chính danh mục này làm danh mục cha");
            }
            IngredientCategory parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new InvalidDataException("Danh mục cha không tồn tại"));
            category.setParentCategory(parent);
        } else {
            // Cho phép xóa danh mục cha (set null) nếu muốn, nhưng trong update request có thể không truyền -> giữ nguyên.
            // DTO không phân biệt null vs không gửi, ta quy ước: nếu không gửi parentCategoryId thì giữ nguyên, nếu gửi null sẽ xóa.
            // Tuy nhiên DTO dùng Integer, không thể phân biệt null do không truyền. Tạm thời ta sẽ chỉ cập nhật nếu field != null.
            // Để set null ta có thể dùng một trick nhưng ít dùng. Ta sẽ bỏ qua việc set null.
        }

        // Xử lý active
        if (request.getActive() != null) {
            if (!request.getActive() && Boolean.TRUE.equals(category.getActive())) {
                // Kiểm tra phụ thuộc trước khi vô hiệu hóa
                validateBeforeDeactivate(category.getId());
            }
            category.setActive(request.getActive());
        }

        categoryRepository.save(category);
        return mapToDetail(category);
    }

    @Override
    @Transactional
    public void delete(IngredientCategoryIdRequest request) {
        IngredientCategory category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));

        if (Boolean.FALSE.equals(category.getActive())) {
            throw new InvalidDataException("Danh mục đã bị vô hiệu hóa trước đó");
        }

        validateBeforeDeactivate(category.getId());
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    public IngredientCategoryDetailResponseDTO getDetail(IngredientCategoryIdRequest request) {
        IngredientCategory category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));
        return mapToDetail(category);
    }

    @Override
    public List<IngredientCategoryOptionDTO> getOptions() {
        List<IngredientCategory> activeCategories = categoryRepository.findAllByActiveTrueOrderByIngredientCategoryNameAsc();
        return activeCategories.stream()
                .map(cat -> IngredientCategoryOptionDTO.builder()
                        .id(cat.getId())
                        .ingredientCategoryCode(cat.getIngredientCategoryCode())
                        .ingredientCategoryName(cat.getIngredientCategoryName())
                        .parentCategoryId(cat.getParentCategory() != null ? cat.getParentCategory().getId() : null)
                        .parentCategoryName(cat.getParentCategory() != null ? cat.getParentCategory().getIngredientCategoryName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------- private --------------------

    private void validateBeforeDeactivate(Integer categoryId) {
        // Kiểm tra có danh mục con active không
        if (categoryRepository.existsByParentCategoryIdAndActiveTrue(categoryId)) {
            throw new InvalidDataException("Không thể vô hiệu hóa vì còn danh mục con đang hoạt động");
        }
        // Kiểm tra có nguyên liệu active thuộc danh mục này không
        if (ingredientRepository.existsByIngredientCategoryIdAndActiveTrue(categoryId)) {
            throw new InvalidDataException("Không thể vô hiệu hóa vì còn nguyên liệu đang hoạt động thuộc danh mục này");
        }
    }

    private IngredientCategoryDetailResponseDTO mapToDetail(IngredientCategory cat) {
        return IngredientCategoryDetailResponseDTO.builder()
                .id(cat.getId())
                .ingredientCategoryCode(cat.getIngredientCategoryCode())
                .ingredientCategoryName(cat.getIngredientCategoryName())
                .createdTime(cat.getCreatedTime())
                .active(cat.getActive())
                .parentCategoryId(cat.getParentCategory() != null ? cat.getParentCategory().getId() : null)
                .parentCategoryName(cat.getParentCategory() != null ? cat.getParentCategory().getIngredientCategoryName() : null)
                .build();
    }

    private IngredientCategoryResponseDTO toResponseDTO(IngredientCategorySearchNativeResultDTO dto) {
        return IngredientCategoryResponseDTO.builder()
                .id(dto.getId())
                .ingredientCategoryCode(dto.getIngredientCategoryCode())
                .ingredientCategoryName(dto.getIngredientCategoryName())
                .createdTime(dto.getCreatedTime())
                .active(dto.getActive())
                .parentCategoryId(dto.getParentCategoryId())
                .parentCategoryName(dto.getParentCategoryName())
                .ingredientCount(dto.getIngredientCount())
                .build();
    }
}
