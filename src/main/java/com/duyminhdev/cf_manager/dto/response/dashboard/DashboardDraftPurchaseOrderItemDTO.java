package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bảng 5: Đơn nhập hàng đang soạn thảo (payment_status = 'DRAFT').
 */
@Data
@Builder
public class DashboardDraftPurchaseOrderItemDTO {
    /** Mã đơn nhập hàng. */
    private String purchaseOrderCode;
    /** Tổng giá trị đơn. */
    private BigDecimal totalAmount;
    /** Thời điểm tạo đơn. */
    private Instant createdAt;
    /** Tên nhà cung cấp. */
    private String supplierName;
}
