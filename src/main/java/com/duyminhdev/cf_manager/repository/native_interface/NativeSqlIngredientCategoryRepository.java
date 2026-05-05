package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientCategorySearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient_category.IngredientCategorySearchRequestDTO;

import java.util.List;

public interface NativeSqlIngredientCategoryRepository {
    PageResponse<List<IngredientCategorySearchNativeResultDTO>> search(IngredientCategorySearchRequestDTO request);
    List<IngredientCategorySearchNativeResultDTO> findAllByCriteria(IngredientCategorySearchRequestDTO request);
}
