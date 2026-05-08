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

    /**
     * Độ sâu tối đa cho phép của cây danh mục.
     * Cấp 1 = root (depth=1), Cấp 2 = con, Cấp 3 = cháu.
     * Thay đổi hằng số này nếu cần mở rộng hoặc thu hẹp cây.
     */
    private static final int MAX_DEPTH = 3;

    /**
     * Giá trị đặc biệt trong {@link IngredientCategoryUpdateRequestDTO#getParentCategoryId()}
     * dùng để báo hiệu "xóa cha, chuyển thành root".
     * Client gửi parentCategoryId = -1 khi muốn bỏ cha.
     */
    private static final int CLEAR_PARENT_SIGNAL = -1;

    private final NativeSqlIngredientCategoryRepository nativeSqlRepo;
    private final IngredientCategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

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
        // 2.1 – Kiểm tra trùng mã (global: kể cả bản ghi inactive)
        if (StringUtils.hasText(request.getIngredientCategoryCode())) {
            categoryRepository.findByIngredientCategoryCode(request.getIngredientCategoryCode())
                    .ifPresent(c -> {
                        throw new InvalidDataException("Mã danh mục đã tồn tại (kể cả bản ghi đã inactive)");
                    });
        }

        IngredientCategory category = new IngredientCategory();
        category.setIngredientCategoryCode(request.getIngredientCategoryCode());
        category.setIngredientCategoryName(request.getIngredientCategoryName());
        category.setCreatedTime(Instant.now());
        category.setActive(true);

        // 2.2 + 2.4 – Gán danh mục cha (nếu có)
        if (request.getParentCategoryId() != null) {
            IngredientCategory parent = requireActiveCategory(request.getParentCategoryId(),
                    "Danh mục cha không tồn tại hoặc đã bị vô hiệu hóa");

            // 2.4 – Kiểm tra độ sâu: node mới sẽ ở depth = depth(parent) + 1
            int newDepth = computeDepth(parent) + 1;
            if (newDepth > MAX_DEPTH) {
                throw new InvalidDataException(
                        "Vượt quá độ sâu tối đa cho phép (" + MAX_DEPTH + " cấp)");
            }

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

        // 2.1 – Cập nhật mã: kiểm tra global unique (ngoại trừ chính nó)
        if (StringUtils.hasText(request.getIngredientCategoryCode())
                && !request.getIngredientCategoryCode().equals(category.getIngredientCategoryCode())) {
            categoryRepository.findByIngredientCategoryCodeAndIdNot(
                    request.getIngredientCategoryCode(), category.getId())
                    .ifPresent(c -> {
                        throw new InvalidDataException("Mã danh mục đã tồn tại (kể cả bản ghi đã inactive)");
                    });
            category.setIngredientCategoryCode(request.getIngredientCategoryCode());
        }

        if (StringUtils.hasText(request.getIngredientCategoryName())) {
            category.setIngredientCategoryName(request.getIngredientCategoryName());
        }

        // 2.2 – Cập nhật danh mục cha
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId() == CLEAR_PARENT_SIGNAL) {
                // Client gửi -1 → xóa cha, trở thành root
                category.setParentCategory(null);
            } else {
                // 2.2 – Không cho phép tự làm cha của chính mình
                if (request.getParentCategoryId().equals(category.getId())) {
                    throw new InvalidDataException("Không thể chọn chính danh mục này làm danh mục cha");
                }

                // 2.2 – Cha phải tồn tại và đang active
                IngredientCategory parent = requireActiveCategory(request.getParentCategoryId(),
                        "Danh mục cha không tồn tại hoặc đã bị vô hiệu hóa");

                // 2.2 – Kiểm tra vòng lặp (circular reference)
                validateNoCircularReference(category.getId(), parent);

                // 2.4 – Kiểm tra độ sâu mới của toàn bộ nhánh con
                int newParentDepth = computeDepth(parent);
                validateSubtreeDepth(category, newParentDepth + 1);

                category.setParentCategory(parent);
            }
        }
        // Nếu request.getParentCategoryId() == null → giữ nguyên cha cũ (không thay đổi)

        // 2.3 – Xử lý kích hoạt / vô hiệu hóa
        if (request.getActive() != null) {
            boolean currentlyActive = Boolean.TRUE.equals(category.getActive());

            if (!request.getActive() && currentlyActive) {
                // Vô hiệu hóa: cascade xuống toàn bộ con/cháu
                // Kiểm tra nguyên liệu active
                validateNoActiveIngredients(category.getId());
                cascadeDeactivate(category);
            } else if (request.getActive() && !currentlyActive) {
                // Kích hoạt lại:
                // 2.3 – Nếu cha đang inactive thì không cho kích hoạt
                if (category.getParentCategory() != null
                        && !Boolean.TRUE.equals(category.getParentCategory().getActive())) {
                    throw new InvalidDataException(
                            "Không thể kích hoạt vì danh mục cha đang bị vô hiệu hóa. " +
                            "Vui lòng kích hoạt danh mục cha trước.");
                }
                category.setActive(true);
            }
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

        // Kiểm tra nguyên liệu active thuộc chính danh mục này
        validateNoActiveIngredients(category.getId());

        // 2.3 – Cascade deactivate toàn bộ nhánh con/cháu rồi mới deactivate chính nó
        cascadeDeactivate(category);
    }

    @Override
    public IngredientCategoryDetailResponseDTO getDetail(IngredientCategoryIdRequest request) {
        IngredientCategory category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));
        return mapToDetail(category);
    }

    @Override
    public List<IngredientCategoryOptionDTO> getOptions() {
        List<IngredientCategory> activeCategories =
                categoryRepository.findAllByActiveTrueOrderByIngredientCategoryNameAsc();
        return activeCategories.stream()
                .map(cat -> IngredientCategoryOptionDTO.builder()
                        .id(cat.getId())
                        .ingredientCategoryCode(cat.getIngredientCategoryCode())
                        .ingredientCategoryName(cat.getIngredientCategoryName())
                        .parentCategoryId(cat.getParentCategory() != null ? cat.getParentCategory().getId() : null)
                        .parentCategoryName(cat.getParentCategory() != null
                                ? cat.getParentCategory().getIngredientCategoryName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Lấy danh mục theo ID và yêu cầu phải đang active.
     */
    private IngredientCategory requireActiveCategory(Integer id, String errorMessage) {
        IngredientCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException(errorMessage));
        if (!Boolean.TRUE.equals(cat.getActive())) {
            throw new InvalidDataException(errorMessage);
        }
        return cat;
    }

    /**
     * Kiểm tra circular reference: duyệt ngược tổ tiên của {@code newParent},
     * nếu gặp {@code categoryId} thì có vòng lặp.
     */
    private void validateNoCircularReference(Integer categoryId, IngredientCategory newParent) {
        IngredientCategory cursor = newParent;
        while (cursor != null) {
            if (cursor.getId().equals(categoryId)) {
                throw new InvalidDataException(
                        "Phát hiện vòng lặp tham chiếu: danh mục con không thể trở thành tổ tiên của cha mới");
            }
            cursor = cursor.getParentCategory();
        }
    }

    /**
     * Tính độ sâu (depth) của một node trong cây.
     * Root = 1, con của root = 2, cháu = 3, ...
     */
    private int computeDepth(IngredientCategory cat) {
        int depth = 1;
        IngredientCategory cursor = cat;
        while (cursor.getParentCategory() != null) {
            depth++;
            cursor = cursor.getParentCategory();
        }
        return depth;
    }

    /**
     * Kiểm tra toàn bộ nhánh con của {@code node} khi đặt nó ở độ sâu
     * {@code nodeDepth} có vượt quá MAX_DEPTH không.
     * Duyệt BFS xuống toàn bộ cây con.
     */
    private void validateSubtreeDepth(IngredientCategory node, int nodeDepth) {
        if (nodeDepth > MAX_DEPTH) {
            throw new InvalidDataException(
                    "Vượt quá độ sâu tối đa cho phép (" + MAX_DEPTH + " cấp) sau khi di chuyển nhánh");
        }
        List<IngredientCategory> children = categoryRepository.findAllByParentCategory_Id(node.getId());
        for (IngredientCategory child : children) {
            validateSubtreeDepth(child, nodeDepth + 1);
        }
    }

    /**
     * Cascade vô hiệu hóa: đặt is_active = 0 cho node và tất cả con/cháu của nó.
     * Duyệt BFS, lưu batch sau khi xử lý từng level.
     */
    private void cascadeDeactivate(IngredientCategory root) {
        Deque<IngredientCategory> queue = new ArrayDeque<>();
        queue.add(root);
        List<IngredientCategory> toSave = new ArrayList<>();

        while (!queue.isEmpty()) {
            IngredientCategory current = queue.poll();
            current.setActive(false);
            toSave.add(current);

            List<IngredientCategory> children =
                    categoryRepository.findAllByParentCategory_Id(current.getId());
            queue.addAll(children);
        }

        categoryRepository.saveAll(toSave);
    }

    /**
     * Kiểm tra không còn nguyên liệu active thuộc danh mục này.
     * Nếu còn → từ chối vô hiệu hóa.
     */
    private void validateNoActiveIngredients(Integer categoryId) {
        if (ingredientRepository.existsByIngredientCategoryIdAndActiveTrue(categoryId)) {
            throw new InvalidDataException(
                    "Không thể vô hiệu hóa vì còn nguyên liệu đang hoạt động thuộc danh mục này");
        }
    }

    // =========================================================================
    // MAPPING
    // =========================================================================

    private IngredientCategoryDetailResponseDTO mapToDetail(IngredientCategory cat) {
        return IngredientCategoryDetailResponseDTO.builder()
                .id(cat.getId())
                .ingredientCategoryCode(cat.getIngredientCategoryCode())
                .ingredientCategoryName(cat.getIngredientCategoryName())
                .createdTime(cat.getCreatedTime())
                .active(cat.getActive())
                .parentCategoryId(cat.getParentCategory() != null ? cat.getParentCategory().getId() : null)
                .parentCategoryName(cat.getParentCategory() != null
                        ? cat.getParentCategory().getIngredientCategoryName() : null)
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
