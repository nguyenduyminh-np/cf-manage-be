package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Biểu đồ 1: Đường – Doanh thu 7 ngày gần nhất.
 * Mỗi item đại diện cho một ngày.
 */
@Data
@Builder
public class DashboardRevenueChartItemDTO {
    /** Ngày (yyyy-MM-dd). */
    private String date;
    /** Tổng doanh thu trong ngày đó. */
    private BigDecimal dailyRevenue;
}
