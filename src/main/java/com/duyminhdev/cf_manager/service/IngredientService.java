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

    /** Lấy chi tiết nguyên liệu kèm *Code từ các bảng liên quan (native query JOIN). */
    IngredientDetailResponseDTO getDetail(IngredientIdRequest request);

    /** Nguồn dữ liệu select danh mục nguyên liệu (code + name). */
    List<IngredientCategorySelectDTO> getDanhSachDanhMuc();

    /** Nguồn dữ liệu select đơn vị tính (code + name). */
    List<UnitSelectDTO> getDanhSachDonVi();

    /** Nguồn dữ liệu select nhà cung cấp (code + name). */
    List<SupplierSelectDTO> getDanhSachNhaCungCap();
}