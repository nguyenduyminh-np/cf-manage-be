package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientSearchRequestDTO;
import java.util.List;
import java.util.Optional;

public interface NativeSqlIngredientRepository {
    PageResponse<List<IngredientSearchNativeResultDTO>> search(IngredientSearchRequestDTO request);
    List<IngredientSearchNativeResultDTO> findAllByCriteria(IngredientSearchRequestDTO request); // cho export

    /**
     * Lấy chi tiết 1 nguyên liệu bằng native query JOIN các bảng liên quan
     * (ingredient_category, supplier, unit) để lấy đủ cả code + name.
     */
    Optional<IngredientDetailNativeResultDTO> findDetailById(Integer id);
}