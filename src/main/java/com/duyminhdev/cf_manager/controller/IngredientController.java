package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientIdRequest;
import com.duyminhdev.cf_manager.dto.request.ingredient.*;
import com.duyminhdev.cf_manager.dto.response.ingredient.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
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
@AdminOrManagerAccess
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

    @PostMapping("/detail")
    public ApiResponse<IngredientDetailResponseDTO> detail(@Valid @RequestBody IngredientIdRequest request) {
        return new ApiResponse<>(200, "GET_INGREDIENT_DETAIL_SUCCESS", ingredientService.getDetail(request));
    }
}