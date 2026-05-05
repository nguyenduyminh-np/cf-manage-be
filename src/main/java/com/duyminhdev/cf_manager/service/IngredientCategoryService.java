package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.ingredient_category.*;
import com.duyminhdev.cf_manager.dto.response.ingredient_category.*;
import java.util.List;

public interface IngredientCategoryService {
    PageResponse<List<IngredientCategoryResponseDTO>> search(IngredientCategorySearchRequestDTO request);
    List<IngredientCategoryExportDTO> exportData(IngredientCategorySearchRequestDTO request);
    IngredientCategoryDetailResponseDTO create(IngredientCategoryCreateRequestDTO request);
    IngredientCategoryDetailResponseDTO update(IngredientCategoryUpdateRequestDTO request);
    void delete(IngredientCategoryIdRequest request);
    IngredientCategoryDetailResponseDTO getDetail(IngredientCategoryIdRequest request);
    List<IngredientCategoryOptionDTO> getOptions();
}
