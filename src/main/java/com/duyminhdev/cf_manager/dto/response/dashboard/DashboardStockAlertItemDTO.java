package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bảng 3: Cảnh báo tồn kho –
 * Lô hàng sắp hết hạn (≤ 7 ngày) HOẶC số lượng ≤ 5.
 */
@Data
@Builder
public class DashboardStockAlertItemDTO {
    /** Tên nguyên liệu. */
    private String ingredientName;
    /** ID lô hàng (batch). */
    private Integer batchId;
    /** Số lượng còn lại trong lô. */
    private BigDecimal quantity;
    /** Thời điểm hết hạn. */
    private Instant expirationAt;
    /** Kho chứa lô hàng này. */
    private String warehouseName;
}
