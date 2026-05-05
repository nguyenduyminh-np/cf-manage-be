package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientSearchRequestDTO;
import java.util.List;

public interface NativeSqlIngredientRepository {
    PageResponse<List<IngredientSearchNativeResultDTO>> search(IngredientSearchRequestDTO request);
    List<IngredientSearchNativeResultDTO> findAllByCriteria(IngredientSearchRequestDTO request); // cho export
}