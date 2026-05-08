package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Biểu đồ 6: Bánh – Cơ cấu công nợ nhà cung cấp.
 * Mỗi item là một nhà cung cấp và tổng nợ chưa thanh toán.
 */
@Data
@Builder
public class DashboardDebtChartItemDTO {
    /** Tên nhà cung cấp. */
    private String supplierName;
    /** Tổng số tiền nợ chưa trả. */
    private BigDecimal debtAmount;
}
