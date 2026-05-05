// PurchaseOrderStatusUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderStatusUpdateRequestDTO {
    @NotNull(message = "Id đơn hàng không được trống")
    private Integer id;
    @NotNull(message = "Trạng thái mới không được trống")
    private String newStatus; // PENDING, APPROVED, COMPLETED, CANCELLED

    // Fix #1: Chỉ cần warehouseId khi newStatus = COMPLETED (supplier lấy từ chính PO)
    private Integer warehouseId;
}