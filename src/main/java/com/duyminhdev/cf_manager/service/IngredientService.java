// IngredientService.java
package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientIdRequest;
import com.duyminhdev.cf_manager.dto.request.ingredient.*;
import com.duyminhdev.cf_manager.dto.response.ingredient.*;
import java.util.List;

public interface IngredientService {
    PageResponse<List<IngredientResponseDTO>> search(IngredientSearchRequestDTO request);
    List<IngredientExportDTO> exportData(IngredientSearchRequestDTO request);
    IngredientDetailResponseDTO create(IngredientCreateRequestDTO request);
    IngredientDetailResponseDTO update(IngredientUpdateRequestDTO request);
    void delete(IngredientIdRequest request);
    IngredientDetailResponseDTO getDetail(IngredientIdRequest request);
}