package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishCategorySearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategorySearchRequestDTO;

import java.util.List;

public interface NativeSqlDishCategoryRepository {
    List<DishCategorySearchNativeResult> searchCategories(
            DishCategorySearchRequestDTO request, int offset, int limit);
    long countCategories(DishCategorySearchRequestDTO request);
}