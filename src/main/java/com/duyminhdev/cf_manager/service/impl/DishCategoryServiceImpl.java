package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryResponseDTO;
import com.duyminhdev.cf_manager.entity.DishCategory;
import com.duyminhdev.cf_manager.mapper.DishCategoryMapper;
import com.duyminhdev.cf_manager.repository.DishCategoryRepository;
import com.duyminhdev.cf_manager.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryRepository dishCategoryRepository;
    private final DishCategoryMapper dishCategoryMapper;

    @Override
    /**
     * Lấy danh sách nhóm món theo điều kiện active để phục vụ menu.
     */
    public List<DishCategoryResponseDTO> getAll(DishCategoryListRequestDTO request) {
        /**
         * Flow:
         * 1. Nếu active = null/true -> lấy category active để hiển thị menu
         * 2. Nếu active = false -> load toàn bộ rồi sort tay
         * 3. Map sang DTO
         */
        List<DishCategory> categories;

        if (request.getActive() == null || request.getActive()) {
            categories = dishCategoryRepository.findAllByActiveTrueOrderByDishCategoryNameAsc();
        } else {
            categories = dishCategoryRepository.findAll().stream()
                    .sorted(Comparator.comparing(DishCategory::getDishCategoryName))
                    .toList();
        }

        return categories.stream()
                .map(dishCategoryMapper::toResponseDTO)
                .toList();
    }
}
