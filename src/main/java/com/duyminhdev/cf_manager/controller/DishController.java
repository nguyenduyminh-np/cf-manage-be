package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish.DishListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish.DishResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishController {

    private final DishService dishService;

    /**
     * Lay danh sach mon, cho phep body optional.
     */
    @PostMapping("/list")
    public ApiResponse<List<DishResponseDTO>> list(
            @RequestBody(required = false) DishListRequestDTO request
    ) {
        /**
         * Flow API dish list:
         * 1. Nhan request list mon (co the null)
         * 2. Neu null thi tao request mac dinh
         * 3. Goi service lay danh sach mon
         * 4. Tra ket qua danh sach cho FE
         */
        DishListRequestDTO safeRequest = request != null ? request : new DishListRequestDTO();
        return new ApiResponse<>(200, "GET_DISH_LIST_SUCCESS", dishService.getAll(safeRequest));
    }

    /**
     * Tim kiem mon theo bo loc va phan trang.
     */
    @PostMapping("/search")
    public ApiResponse<PageResponse<List<DishResponseDTO>>> search(
            @Valid @RequestBody DishSearchRequestDTO request
    ) {
        /**
         * Flow API dish search:
         * 1. Nhan bo loc tim kiem mon
         * 2. Goi service search co phan trang
         * 3. Tra page result cho FE
         */
        return new ApiResponse<>(200, "SEARCH_DISH_SUCCESS", dishService.search(request));
    }
}
