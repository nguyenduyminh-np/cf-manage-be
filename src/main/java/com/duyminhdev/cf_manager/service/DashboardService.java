package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.response.dashboard.*;

import java.util.List;

/**
 * Service cung cấp dữ liệu cho tất cả các widget trên Dashboard.
 */
public interface DashboardService {

    /** Lấy toàn bộ 8 KPI Card trong một lần gọi. */
    DashboardKpiDTO getKpi();

    /** Biểu đồ 1 – Doanh thu 7 ngày gần nhất. */
    List<DashboardRevenueChartItemDTO> getRevenueLast7Days();

    /** Biểu đồ 2 – Số đơn theo giờ trong ngày hôm nay. */
    List<DashboardOrderByHourItemDTO> getOrdersByHourToday();

    /** Biểu đồ 3 – Phân bổ trạng thái đơn hàng hôm nay. */
    List<DashboardOrderStatusChartItemDTO> getOrderStatusDistributionToday();

    /** Biểu đồ 4 – Top 5 món bán chạy hôm nay. */
    List<DashboardTopDishItemDTO> getTop5DishesToday();

    /** Biểu đồ 5 – Bàn trống / có khách theo tầng. */
    List<DashboardTableByFloorItemDTO> getTableStatusByFloor();

    /** Biểu đồ 6 – Cơ cấu công nợ nhà cung cấp. */
    List<DashboardDebtChartItemDTO> getDebtBySupplier();

    /** Bảng 1 – Đơn hàng đang chế biến hôm nay. */
    List<DashboardProcessingOrderItemDTO> getProcessingOrdersToday();

    /** Bảng 2 – Booking sắp đến trong 2 giờ tới. */
    List<DashboardUpcomingBookingItemDTO> getUpcomingBookings();

    /** Bảng 3 – Cảnh báo tồn kho (hết hạn sớm / sắp hết). */
    List<DashboardStockAlertItemDTO> getStockAlerts();

    /** Bảng 4 – Hóa đơn chưa thanh toán. */
    List<DashboardPendingInvoiceItemDTO> getPendingInvoices();

    /** Bảng 5 – Đơn nhập hàng đang soạn (DRAFT). */
    List<DashboardDraftPurchaseOrderItemDTO> getDraftPurchaseOrders();

    /** Bảng 6 – Đặt bàn chờ xác nhận (status = PENDING). */
    List<DashboardPendingBookingItemDTO> getPendingBookings();
}
