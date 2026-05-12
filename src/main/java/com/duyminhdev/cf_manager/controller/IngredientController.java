package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientIdRequest;
import com.duyminhdev.cf_manager.dto.request.ingredient.*;
import com.duyminhdev.cf_manager.dto.response.ingredient.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOnlyAccess;
import com.duyminhdev.cf_manager.service.IngredientService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredient")
@RequiredArgsConstructor
@AdminOnlyAccess
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<IngredientResponseDTO>>> search(
            @Valid @RequestBody IngredientSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_INGREDIENT_SUCCESS", ingredientService.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody IngredientSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<IngredientExportDTO> items = ingredientService.exportData(request);
        String fileName = "DANH_SACH_NGUYEN_LIEU_" + System.currentTimeMillis() + ".xlsx";
        ExcelUtils.export(response, IngredientExportDTO.class, items, fileName, "DANH SÁCH NGUYÊN LIỆU");
    }

    @PostMapping("/create")
    public ApiResponse<IngredientDetailResponseDTO> create(
            @Valid @RequestBody IngredientCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_INGREDIENT_SUCCESS", ingredientService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<IngredientDetailResponseDTO> update(
            @Valid @RequestBody IngredientUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_INGREDIENT_SUCCESS", ingredientService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IngredientIdRequest request) {
        ingredientService.delete(request);
        return new ApiResponse<>(200, "DELETE_INGREDIENT_SUCCESS");
    }

    /**
     * Lấy chi tiết nguyên liệu kèm đầy đủ thông tin từ bảng liên kết:
     * ingredientCategoryCode, ingredientCategoryName,
     * supplierCode, supplierName, unitCode, unitName.
     */
    @PostMapping("/detail")
    public ApiResponse<IngredientDetailResponseDTO> detail(@Valid @RequestBody IngredientIdRequest request) {
        return new ApiResponse<>(200, "GET_INGREDIENT_DETAIL_SUCCESS", ingredientService.getDetail(request));
    }

    // ============================================================
    // Nguồn dữ liệu cho dropdown khi thêm mới / cập nhật nguyên liệu
    // ============================================================

    /**
     * Lấy toàn bộ danh mục nguyên liệu đang hoạt động.
     * Mỗi item gồm: ingredient_category_code | ingredient_category_name
     */
    @PostMapping("/danh-sach-danh-muc")
    public ApiResponse<List<IngredientCategorySelectDTO>> danhSachDanhMuc() {
        return new ApiResponse<>(200, "GET_DANH_MUC_SUCCESS", ingredientService.getDanhSachDanhMuc());
    }

    /**
     * Lấy toàn bộ đơn vị tính đang hoạt động.
     * Mỗi item gồm: unit_code | unit_name
     */
    @PostMapping("/danh-sach-don-vi")
    public ApiResponse<List<UnitSelectDTO>> danhSachDonVi() {
        return new ApiResponse<>(200, "GET_DON_VI_SUCCESS", ingredientService.getDanhSachDonVi());
    }

    /**
     * Lấy toàn bộ nhà cung cấp đang hoạt động.
     * Mỗi item gồm: supplier_code | supplier_name
     */
    @PostMapping("/danh-sach-nha-cung-cap")
    public ApiResponse<List<SupplierSelectDTO>> danhSachNhaCungCap() {
        return new ApiResponse<>(200, "GET_NHA_CUNG_CAP_SUCCESS", ingredientService.getDanhSachNhaCungCap());
    }
}