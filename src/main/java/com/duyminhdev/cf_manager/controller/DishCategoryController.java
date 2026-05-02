package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish_category.*;
import com.duyminhdev.cf_manager.dto.response.dish_category.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishCategoryService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dish-category")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishCategoryController {

    private final DishCategoryService dishCategoryService;

    @PostMapping("/options")
    public ApiResponse<List<DishCategoryOptionDTO>> getOptions() {
        return new ApiResponse<>(200, "GET_OPTIONS_SUCCESS", dishCategoryService.getOptions());
    }

    @PostMapping("/list")
    public ApiResponse<List<DishCategoryResponseDTO>> list(
            @RequestBody(required = false) DishCategoryListRequestDTO request) {
        DishCategoryListRequestDTO safe = request != null ? request : new DishCategoryListRequestDTO();
        return new ApiResponse<>(200, "GET_DISH_CATEGORY_LIST_SUCCESS", dishCategoryService.getAll(safe));
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<DishCategoryListItemDTO>>> search(
            @RequestBody DishCategorySearchRequestDTO request) {
        PageResponse<List<DishCategoryListItemDTO>> result = dishCategoryService.search(request);
        return new ApiResponse<>(200, "SEARCH_SUCCESS", result);
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody DishCategorySearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<DishCategoryExportDTO> items = dishCategoryService.exportData(request);

        String fileName = "DANH_SACH_DANH_MUC_MON_AN_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH DANH MỤC MÓN ĂN";

        ExcelUtils.export(response, DishCategoryExportDTO.class, items, fileName, title);
    }

    @PostMapping("/create")
    public ApiResponse<DishCategoryDetailResponseDTO> create(
            @Valid @RequestBody DishCategoryCreateRequestDTO request) {
        DishCategoryDetailResponseDTO dto = dishCategoryService.create(request);
        return new ApiResponse<>(201, "CREATE_SUCCESS", dto);
    }

    @PostMapping("/update")
    public ApiResponse<DishCategoryDetailResponseDTO> update(
            @Valid @RequestBody DishCategoryUpdateRequestDTO request) {
        DishCategoryDetailResponseDTO dto = dishCategoryService.update(request);
        return new ApiResponse<>(200, "UPDATE_SUCCESS", dto);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        dishCategoryService.delete(id);
        return new ApiResponse<>(200, "DELETE_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<DishCategoryDetailResponseDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        DishCategoryDetailResponseDTO dto = dishCategoryService.getDetail(id);
        return new ApiResponse<>(200, "DETAIL_SUCCESS", dto);
    }
}