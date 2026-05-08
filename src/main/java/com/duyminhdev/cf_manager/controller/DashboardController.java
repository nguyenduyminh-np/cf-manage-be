package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.response.dashboard.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class DashboardController {

    private final DashboardService dashboardService;

    // =========================================================================
    // KPI
    // =========================================================================

    /**
     * Lấy toàn bộ 8 chỉ số KPI Card trong một lần gọi.
     * FE không cần gọi 8 request riêng lẻ.
     */
    @PostMapping("/kpi")
    public ApiResponse<DashboardKpiDTO> getKpi() {
        return new ApiResponse<>(200, "GET_DASHBOARD_KPI_SUCCESS", dashboardService.getKpi());
    }

    // =========================================================================
    // CHARTS
    // =========================================================================

    /**
     * Biểu đồ 1 – Đường – Doanh thu 7 ngày gần nhất.
     * Trả về danh sách {date, dailyRevenue}.
     */
    @PostMapping("/chart/revenue-last-7-days")
    public ApiResponse<List<DashboardRevenueChartItemDTO>> getRevenueLast7Days() {
        return new ApiResponse<>(200, "GET_REVENUE_LAST_7_DAYS_SUCCESS",
                dashboardService.getRevenueLast7Days());
    }

    /**
     * Biểu đồ 2 – Cột – Số đơn hàng theo giờ trong ngày hôm nay.
     * Trả về danh sách {hour (0-23), orderCount}.
     */
    @PostMapping("/chart/orders-by-hour")
    public ApiResponse<List<DashboardOrderByHourItemDTO>> getOrdersByHourToday() {
        return new ApiResponse<>(200, "GET_ORDERS_BY_HOUR_SUCCESS",
                dashboardService.getOrdersByHourToday());
    }

    /**
     * Biểu đồ 3 – Bánh – Phân bổ trạng thái đơn hàng hôm nay.
     * Trả về danh sách {status (tên hiển thị), orderCount}.
     */
    @PostMapping("/chart/order-status-distribution")
    public ApiResponse<List<DashboardOrderStatusChartItemDTO>> getOrderStatusDistributionToday() {
        return new ApiResponse<>(200, "GET_ORDER_STATUS_DISTRIBUTION_SUCCESS",
                dashboardService.getOrderStatusDistributionToday());
    }

    /**
     * Biểu đồ 4 – Ngang – Top 5 món bán chạy hôm nay.
     * Chỉ tính đơn không bị hủy. Trả về danh sách {dishName, totalQuantity}.
     */
    @PostMapping("/chart/top-dishes")
    public ApiResponse<List<DashboardTopDishItemDTO>> getTop5DishesToday() {
        return new ApiResponse<>(200, "GET_TOP_DISHES_SUCCESS",
                dashboardService.getTop5DishesToday());
    }

    /**
     * Biểu đồ 5 – Cột chồng – Trạng thái bàn theo tầng.
     * Trả về danh sách {floor, occupied, available}.
     */
    @PostMapping("/chart/table-status-by-floor")
    public ApiResponse<List<DashboardTableByFloorItemDTO>> getTableStatusByFloor() {
        return new ApiResponse<>(200, "GET_TABLE_STATUS_BY_FLOOR_SUCCESS",
                dashboardService.getTableStatusByFloor());
    }

    /**
     * Biểu đồ 6 – Bánh – Cơ cấu công nợ nhà cung cấp.
     * Trả về danh sách {supplierName, debtAmount} sắp xếp theo nợ giảm dần.
     */
    @PostMapping("/chart/debt-by-supplier")
    public ApiResponse<List<DashboardDebtChartItemDTO>> getDebtBySupplier() {
        return new ApiResponse<>(200, "GET_DEBT_BY_SUPPLIER_SUCCESS",
                dashboardService.getDebtBySupplier());
    }

    // =========================================================================
    // QUICK LISTS (Bảng danh sách nhanh)
    // =========================================================================

    /**
     * Bảng 1 – Đơn hàng đang chế biến hôm nay.
     * Trả về danh sách {orderId, tableName, createdAt, itemsSummary}.
     */
    @PostMapping("/table/processing-orders")
    public ApiResponse<List<DashboardProcessingOrderItemDTO>> getProcessingOrdersToday() {
        return new ApiResponse<>(200, "GET_PROCESSING_ORDERS_SUCCESS",
                dashboardService.getProcessingOrdersToday());
    }

    /**
     * Bảng 2 – Booking sắp đến trong 2 giờ tới.
     * Không bao gồm booking CANCELLED hoặc EXPIRED.
     */
    @PostMapping("/table/upcoming-bookings")
    public ApiResponse<List<DashboardUpcomingBookingItemDTO>> getUpcomingBookings() {
        return new ApiResponse<>(200, "GET_UPCOMING_BOOKINGS_SUCCESS",
                dashboardService.getUpcomingBookings());
    }

    /**
     * Bảng 3 – Cảnh báo tồn kho:
     * Lô hàng sắp hết hạn (≤ 7 ngày) HOẶC số lượng ≤ 5.
     */
    @PostMapping("/table/stock-alerts")
    public ApiResponse<List<DashboardStockAlertItemDTO>> getStockAlerts() {
        return new ApiResponse<>(200, "GET_STOCK_ALERTS_SUCCESS",
                dashboardService.getStockAlerts());
    }

    /**
     * Bảng 4 – Hóa đơn chưa thanh toán.
     * Sắp xếp theo thời gian tạo tăng dần (ưu tiên hóa đơn đợi lâu nhất).
     */
    @PostMapping("/table/pending-invoices")
    public ApiResponse<List<DashboardPendingInvoiceItemDTO>> getPendingInvoices() {
        return new ApiResponse<>(200, "GET_PENDING_INVOICES_SUCCESS",
                dashboardService.getPendingInvoices());
    }

    /**
     * Bảng 5 – Đơn nhập hàng đang soạn thảo (DRAFT).
     * Sắp xếp theo thời gian tạo giảm dần.
     */
    @PostMapping("/table/draft-purchase-orders")
    public ApiResponse<List<DashboardDraftPurchaseOrderItemDTO>> getDraftPurchaseOrders() {
        return new ApiResponse<>(200, "GET_DRAFT_PURCHASE_ORDERS_SUCCESS",
                dashboardService.getDraftPurchaseOrders());
    }
}
