package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.*;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.OrderHistoryExportDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.OrderHistoryResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishOrderService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dish-order")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DishOrderController {

    private final DishOrderService dishOrderService;

    /**
     * Nhân viên phục vụ sử dụng ứng dụng để ghi nhận các món ăn/đồ uống mà khách yêu cầu tại một bàn cụ thể.
     * Hệ thống sẽ lưu đơn hàng với trạng thái "Đang chế biến" và tính toán tổng tiền dựa trên số lượng và đơn giá hiện tại.
     */
    @PostMapping("/create")
    public ApiResponse<DishOrderResponseDTO> create(
            @Valid @RequestBody DishOrderCreateRequestDTO request
    ) {
        return new ApiResponse<>(200, "CREATE_DISH_ORDER_SUCCESS", dishOrderService.create(request));
    }

    /**
     * Trong quá trình phục vụ,nhân viên có thể cần thay đổi danh sách món đã gọi (thêm, bớt, sửa số lượng), chuyển bàn cho khách,
     * hoặc thay đổi trạng thái đơn hàng.
     * Hệ thống cho phép cập nhật toàn bộ đơn hàng và tự động tính lại tổng tiền.
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

    @PostMapping("/order-history")
    public ApiResponse<PageResponse<List<OrderHistoryResponseDTO>>> searchOrderHistoryByTable(
            @Valid @RequestBody OrderHistorySearchRequestDTO request
    ) {
        return new ApiResponse<>(200, "SEARCH_DISH_ORDER_HISTORY_SUCCESS", dishOrderService.searchOrderHistoryByTable(request));
    }

    @PostMapping("/order-history/export")
    public void exportOrderHistoryByTable(@Valid @RequestBody OrderHistorySearchRequestDTO request,
                                          HttpServletResponse response) throws IOException {
        List<OrderHistoryExportDTO> items = dishOrderService.exportOrderHistoryByTable(request);

        String fileName = "DANH_SACH_LICH_SU_ORDER_MON_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH LỊCH SỬ ORDER MÓN";

        ExcelUtils.export(response, OrderHistoryExportDTO.class, items, fileName, title);
    }

    @PostMapping("/dishes-for-pos-order-dishes")
    public ApiResponse<PageResponse<List<DishSearchNativeResultDTO>>> searchDishesForPosOrderDishes(
            @Valid @RequestBody DishSearchRequestDTO request
    ) {
        return new ApiResponse<>(200, "SEARCH_DISHES_FOR_POS_ORDER_DISHES_SUCCESS", dishOrderService.searchDishesForPosOrderDishes(request));
    }

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
     * Cap nhat trang thai order mon.
     */
    @PostMapping("/update-status")
    public ApiResponse<Boolean> updateStatus(
            @Valid @RequestBody DishOrderStatusUpdateRequestDTO request
    ) {
        return new ApiResponse<>(200, "UPDATE_DISH_ORDER_STATUS_SUCCESS", dishOrderService.updateStatus(request));
    }

    /**
     * Preview thông tin thanh toán (không có voucher).
     */
    @PostMapping("/payment/preview")
    public ApiResponse<?> getPaymentPreview(
            @NotNull @Positive @RequestParam Integer orderId
    ) {
        return new ApiResponse<>(200, "GET_PAYMENT_PREVIEW_SUCCESS",
                dishOrderService.getPaymentPreview(orderId));
    }

    /**
     * Preview thông tin thanh toán kèm preview mã voucher (READ-ONLY).
     * Nhân viên nhập mã voucher trước khi bấm thanh toán;
     * endpoint này trả về số tiền giảm và finalAmount để FE hiển thị.
     * Không tăng usedCount, không ghi bất kỳ dữ liệu nào.
     */
    @PostMapping("/payment/preview-with-voucher")
    public ApiResponse<?> getPaymentPreviewWithVoucher(
            @NotNull @Positive @RequestParam Integer orderId,
            @RequestParam(required = false) String voucherCode
    ) {
        return new ApiResponse<>(200, "GET_PAYMENT_PREVIEW_WITH_VOUCHER_SUCCESS",
                dishOrderService.getPaymentPreviewWithVoucher(orderId, voucherCode));
    }
}
