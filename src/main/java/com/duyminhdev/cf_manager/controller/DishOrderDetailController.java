package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByOrderRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByTableRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishGroupedByTableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishOrderDetailResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishOrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dish-order-detail")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishOrderDetailController {

    private final DishOrderDetailService dishOrderDetailService;

    /**
     * Lay danh sach detail cua mot order mon.
     */
    @PostMapping("/list-by-order")
    public ApiResponse<List<DishOrderDetailResponseDTO>> listByOrder(
            @Valid @RequestBody DishOrderDetailListByOrderRequestDTO request
    ) {
        /**
         * Flow API dish-order-detail list-by-order:
         * 1. Nhan dishOrderId can xem chi tiet
         * 2. Goi service lay cac dong detail theo order
         * 3. Tra danh sach detail cho FE
         */
        return new ApiResponse<>(200, "GET_DISH_ORDER_DETAIL_SUCCESS", dishOrderDetailService.listByOrder(request));
    }

    /**
     * Lay danh sach mon da gom theo ban cho checkout.
     */
    @PostMapping("/list-by-table")
    public ApiResponse<List<DishGroupedByTableResponseDTO>> listByTable(
            @Valid @RequestBody DishOrderDetailListByTableRequestDTO request
    ) {
        /**
         * Flow API dish-order-detail list-by-table:
         * 1. Nhan tableId can tinh tong checkout
         * 2. Goi service aggregate mon theo ban
         * 3. Tra danh sach mon da gom cho FE
         */
        return new ApiResponse<>(200, "GET_GROUPED_DISH_BY_TABLE_SUCCESS", dishOrderDetailService.listByTable(request));
    }
}
