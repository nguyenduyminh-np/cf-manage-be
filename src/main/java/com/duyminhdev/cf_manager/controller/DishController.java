package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish.*;
import com.duyminhdev.cf_manager.dto.response.dish.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishController {

    private final DishService dishService;

    @PostMapping("/list")
    public ApiResponse<List<DishResponseDTO>> list(
            @RequestBody(required = false) DishListRequestDTO request) {
        DishListRequestDTO safeRequest = request != null ? request : new DishListRequestDTO();
        return new ApiResponse<>(200, "GET_DISH_LIST_SUCCESS", dishService.getAll(safeRequest));
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<DishResponseDTO>>> search(
            @Valid @RequestBody DishSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_DISH_SUCCESS", dishService.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody DishSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<DishExportDTO> items = dishService.exportData(request);

        String fileName = "DANH_SACH_MON_AN_" + System.currentTimeMillis() + ".xlsx";

        // Tiêu đề hiển thị trong Excel
        String title = "DANH SÁCH MÓN ĂN";

        ExcelUtils.export(response, DishExportDTO.class, items, fileName, title);
    }

    @PostMapping("/create")
    public ApiResponse<DishDetailResponseDTO> create(
            @Valid @RequestBody DishCreateRequestDTO request) {
        DishDetailResponseDTO dto = dishService.create(request);
        return new ApiResponse<>(201, "CREATE_DISH_SUCCESS", dto);
    }

    @PostMapping("/update")
    public ApiResponse<DishDetailResponseDTO> update(
            @Valid @RequestBody DishUpdateRequestDTO request) {
        DishDetailResponseDTO dto = dishService.update(request);
        return new ApiResponse<>(200, "UPDATE_DISH_SUCCESS", dto);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        dishService.delete(id);
        return new ApiResponse<>(200, "DELETE_DISH_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<DishDetailResponseDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        DishDetailResponseDTO dto = dishService.getDetail(id);
        return new ApiResponse<>(200, "GET_DISH_DETAIL_SUCCESS", dto);
    }
}