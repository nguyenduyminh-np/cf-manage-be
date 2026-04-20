package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategoryListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dish-category")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishCategoryController {

    private final DishCategoryService dishCategoryService;

    /**
     * Lay danh sach loai mon, cho phep body optional.
     */
    @PostMapping("/list")
    public ApiResponse<List<DishCategoryResponseDTO>> list(
            @RequestBody(required = false) DishCategoryListRequestDTO request
    ) {
        /**
         * Flow API dish category list:
         * 1. Nhan request list category (co the null)
         * 2. Neu null thi tao request mac dinh
         * 3. Goi service lay danh sach category
         * 4. Tra ket qua cho FE
         */
        DishCategoryListRequestDTO safeRequest = request != null ? request : new DishCategoryListRequestDTO();
        return new ApiResponse<>(200, "GET_DISH_CATEGORY_LIST_SUCCESS", dishCategoryService.getAll(safeRequest));
    }
}
