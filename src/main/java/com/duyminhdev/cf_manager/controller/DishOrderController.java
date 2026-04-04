package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderListByTableRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;
import com.duyminhdev.cf_manager.service.DishOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dish-order")
@RequiredArgsConstructor
public class DishOrderController {

    private final DishOrderService dishOrderService;

    /**
     * Lay lich su order mon theo ban.
     */
    @PostMapping("/list-by-table")
    public ApiResponse<List<DishOrderResponseDTO>> listByTable(
            @Valid @RequestBody DishOrderListByTableRequestDTO request
    ) {
        /**
         * Flow API dish order list-by-table:
         * 1. Nhan tableId can xem lich su order
         * 2. Goi service lay danh sach order theo ban
         * 3. Tra ket qua cho FE
         */
        return new ApiResponse<>(200, "GET_DISH_ORDER_HISTORY_SUCCESS", dishOrderService.listByTable(request));
    }

    /**
     * Tao order mon moi cho ban.
     */
    @PostMapping("/create")
    public ApiResponse<DishOrderResponseDTO> create(
            @Valid @RequestBody DishOrderCreateRequestDTO request
    ) {
        /**
         * Flow API dish order create:
         * 1. Nhan header order va danh sach mon
         * 2. Goi service tao order va detail
         * 3. Tra order da tao
         */
        return new ApiResponse<>(200, "CREATE_DISH_ORDER_SUCCESS", dishOrderService.create(request));
    }

    /**
     * Cap nhat order mon hien co.
     */
    @PostMapping("/update")
    public ApiResponse<DishOrderResponseDTO> update(
            @Valid @RequestBody DishOrderUpdateRequestDTO request
    ) {
        /**
         * Flow API dish order update:
         * 1. Nhan dishOrderId va du lieu cap nhat
         * 2. Goi service cap nhat order + detail
         * 3. Tra ket qua sau cap nhat
         */
        return new ApiResponse<>(200, "UPDATE_DISH_ORDER_SUCCESS", dishOrderService.update(request));
    }

    /**
     * Cap nhat trang thai order mon.
     */
    @PostMapping("/update-status")
    public ApiResponse<Boolean> updateStatus(
            @Valid @RequestBody DishOrderStatusUpdateRequestDTO request
    ) {
        /**
         * Flow API dish order update-status:
         * 1. Nhan dishOrderId va status moi
         * 2. Goi service doi status order
         * 3. Tra ket qua cho FE
         */
        return new ApiResponse<>(200, "UPDATE_DISH_ORDER_STATUS_SUCCESS", dishOrderService.updateStatus(request));
    }
}
