package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response DTO gom tất cả 8 KPI Card vào một object để giảm số lần gọi API.
 * FE chỉ cần một POST /api/v1/dashboard/kpi để lấy toàn bộ chỉ số.
 */
@Data
@Builder
public class DashboardKpiDTO {

    /** KPI 1 – Doanh thu hôm nay (invoice đã PAID). */
    private BigDecimal revenueToday;

    /** KPI 2 – Tổng đơn hàng hôm nay. */
    private Integer totalOrdersToday;

    /** KPI 2 – Số đơn hàng đã thanh toán hôm nay. */
    private Integer paidOrdersToday;

    /** KPI 3 – Số bàn đang phục vụ (OCCUPIED). */
    private Integer occupiedTables;

    /** KPI 4 – Nhân viên đang hoạt động (role STAFF / CASHIER). */
    private Integer activeStaff;

    /** KPI 5 – Đơn đang chờ bếp chế biến (PROCESSING) hôm nay. */
    private Integer processingOrders;

    /** KPI 6 – Tổng nợ nhà cung cấp chưa thanh toán. */
    private BigDecimal totalSupplierDebt;

    /** KPI 7 – Số lô tồn kho sắp hết hạn trong 30 ngày tới. */
    private Integer expiringSoonStock;

    /** KPI 8 – Số booking sắp đến trong 2 giờ tới. */
    private Integer upcomingBookings;
}
