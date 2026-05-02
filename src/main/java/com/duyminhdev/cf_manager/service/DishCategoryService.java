package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategorySearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryExportDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryListItemDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryOptionDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryResponseDTO;

import java.util.List;

public interface DishCategoryService {

    /**
     * Lấy danh sách loại món để hiển thị nhóm menu.
     */
    List<DishCategoryResponseDTO> getAll(DishCategoryListRequestDTO request);

    PageResponse<List<DishCategoryListItemDTO>> search(DishCategorySearchRequestDTO request);
    List<DishCategoryExportDTO> exportData(DishCategorySearchRequestDTO request);
    DishCategoryDetailResponseDTO create(DishCategoryCreateRequestDTO request);
    DishCategoryDetailResponseDTO update(DishCategoryUpdateRequestDTO request);
    void delete(Integer id);   // xóa mềm
    DishCategoryDetailResponseDTO getDetail(Integer id);

    /**
     * Lấy danh sách danh mục đang active (id, code, name) để làm dropdown
     */
    List<DishCategoryOptionDTO> getOptions();
}
