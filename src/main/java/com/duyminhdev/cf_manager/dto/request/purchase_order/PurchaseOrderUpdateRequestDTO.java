// PurchaseOrderUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class PurchaseOrderUpdateRequestDTO {
    @NotNull(message = "Id không được trống")
    private Integer id;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private Instant orderDate;
    private Integer supplierId;     // Fix #1 – nullable, chỉ cập nhật nếu có
    private Integer warehouseId;    // Fix #1 – nullable
    private List<Detail> details;   // danh sách mới (merge theo id)

    @Data
    public static class Detail {
        private Integer id;   // null -> thêm mới, != null -> cập nhật

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