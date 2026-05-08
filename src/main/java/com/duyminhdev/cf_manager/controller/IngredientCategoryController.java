package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.ingredient_category.*;
import com.duyminhdev.cf_manager.dto.response.ingredient_category.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.IngredientCategoryService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredient-category")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class IngredientCategoryController {

    private final IngredientCategoryService service;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<IngredientCategoryResponseDTO>>> search(
            @Valid @RequestBody IngredientCategorySearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_INGREDIENT_CATEGORY_SUCCESS", service.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody IngredientCategorySearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<IngredientCategoryExportDTO> items = service.exportData(request);
        String fileName = "DANH_MUC_NGUYEN_LIEU_" + System.currentTimeMillis() + ".xlsx";
        ExcelUtils.export(response, IngredientCategoryExportDTO.class, items, fileName, "DANH MỤC NGUYÊN LIỆU");
    }

    @PostMapping("/create")
    public ApiResponse<IngredientCategoryDetailResponseDTO> create(
            @Valid @RequestBody IngredientCategoryCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_INGREDIENT_CATEGORY_SUCCESS", service.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<IngredientCategoryDetailResponseDTO> update(
            @Valid @RequestBody IngredientCategoryUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_INGREDIENT_CATEGORY_SUCCESS", service.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IngredientCategoryIdRequest request) {
        service.delete(request);
        return new ApiResponse<>(200, "DELETE_INGREDIENT_CATEGORY_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<IngredientCategoryDetailResponseDTO> detail(
            @Valid @RequestBody IngredientCategoryIdRequest request) {
        return new ApiResponse<>(200, "GET_INGREDIENT_CATEGORY_DETAIL_SUCCESS", service.getDetail(request));
    }

    @GetMapping("/options")
    public ApiResponse<List<IngredientCategoryOptionDTO>> options() {
        return new ApiResponse<>(200, "INGREDIENT_CATEGORY_OPTIONS_SUCCESS", service.getOptions());
    }
}
