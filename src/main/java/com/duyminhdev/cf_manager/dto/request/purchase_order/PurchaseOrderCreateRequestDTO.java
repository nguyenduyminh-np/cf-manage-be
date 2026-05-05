// PurchaseOrderCreateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class PurchaseOrderCreateRequestDTO {
    private String purchaseOrderCode;   // optional, tự sinh nếu null

    @NotNull(message = "Tổng tiền không được trống")
    private BigDecimal totalPrice;

    @NotNull(message = "Trạng thái thanh toán không được trống")
    private String paymentStatus;   // DRAFT, PENDING, APPROVED

    private Instant orderDate;

    @NotNull(message = "Nhà cung cấp không được trống")
    private Integer supplierId;         // Fix #1

    @NotNull(message = "Kho nhận hàng không được trống")
    private Integer warehouseId;        // Fix #1

    @NotEmpty(message = "Chi tiết đơn hàng không được trống")
    private List<Detail> details;

    @Data
    public static class Detail {
        @NotNull(message = "Mã nguyên liệu không được trống")
        private Integer ingredientId;

        @NotNull(message = "Số lượng không được trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")   // Fix #10
        private Integer quantity;

        @NotNull(message = "Đơn giá không được trống")
        @DecimalMin(value = "0", inclusive = false, message = "Đơn giá phải lớn hơn 0")  // Fix #10
        private BigDecimal unitPrice;
    }
}