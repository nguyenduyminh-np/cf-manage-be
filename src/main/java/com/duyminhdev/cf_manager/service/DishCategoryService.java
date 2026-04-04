package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryResponseDTO;

import java.util.List;

public interface DishCategoryService {

    /**
     * Lấy danh sách loại món để hiển thị nhóm menu.
     */
    List<DishCategoryResponseDTO> getAll(DishCategoryListRequestDTO request);
}
