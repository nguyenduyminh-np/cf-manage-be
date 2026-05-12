package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.response.dashboard.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository thực thi các Native SQL query phục vụ Dashboard.
 * Tất cả query đều chỉ đọc (read-only), không thay đổi dữ liệu.
 */
public interface NativeSqlDashboardRepository {

    // -----------------------------------------------------------------------
    // KPI queries (scalar values)
    // -----------------------------------------------------------------------

    /** KPI 1 – Tổng doanh thu hóa đơn PAID hôm nay. */
    BigDecimal getRevenueToday();

    /** KPI 2 – [0] totalOrders, [1] paidOrders hôm nay. */
    int[] getOrderCountsToday();

    /** KPI 3 – Số bàn đang có khách (OCCUPIED). */
    int getOccupiedTableCount();

    /** KPI 4 – Số nhân viên / thu ngân đang active. */
    int getActiveStaffCount();

    /** KPI 5 – Đơn hàng đang chờ bếp (PROCESSING) hôm nay. */
    int getProcessingOrderCount();

    /** KPI 6 – Tổng nợ nhà cung cấp chưa thanh toán. */
    BigDecimal getTotalSupplierDebt();

    /** KPI 7 – Số lô tồn kho sắp hết hạn trong 30 ngày. */
    int getExpiringSoonStockCount();

    /** KPI 8 – Booking sắp đến trong 2 giờ tới. */
    int getUpcomingBookingCount();

    // -----------------------------------------------------------------------
    // Chart queries (list)
    // -----------------------------------------------------------------------

    /** Biểu đồ 1 – Doanh thu 7 ngày gần nhất. */
    List<DashboardRevenueChartItemDTO> getRevenueLast7Days();

    /** Biểu đồ 2 – Số đơn theo giờ trong ngày hôm nay. */
    List<DashboardOrderByHourItemDTO> getOrdersByHourToday();

    /** Biểu đồ 3 – Phân bổ trạng thái đơn hàng hôm nay. */
    List<DashboardOrderStatusChartItemDTO> getOrderStatusDistributionToday();

    /** Biểu đồ 4 – Top 5 món bán chạy hôm nay. */
    List<DashboardTopDishItemDTO> getTop5DishesToday();

    /** Biểu đồ 5 – Bàn trống / bàn có khách theo tầng. */
    List<DashboardTableByFloorItemDTO> getTableStatusByFloor();

    /** Biểu đồ 6 – Cơ cấu công nợ nhà cung cấp. */
    List<DashboardDebtChartItemDTO> getDebtBySupplier();

    // -----------------------------------------------------------------------
    // Quick-list queries (list)
    // -----------------------------------------------------------------------

    /** Bảng 1 – Đơn hàng đang chế biến hôm nay. */
    List<DashboardProcessingOrderItemDTO> getProcessingOrdersToday();

    /** Bảng 2 – Booking sắp đến trong 2 giờ tới. */
    List<DashboardUpcomingBookingItemDTO> getUpcomingBookings();

    /** Bảng 3 – Cảnh báo tồn kho (hết hạn ≤7 ngày HOẶC số lượng ≤5). */
    List<DashboardStockAlertItemDTO> getStockAlerts();

    /** Bảng 4 – Hóa đơn chưa thanh toán. */
    List<DashboardPendingInvoiceItemDTO> getPendingInvoices();

    /** Bảng 5 – Đơn nhập hàng đang soạn (DRAFT). */
    List<DashboardDraftPurchaseOrderItemDTO> getDraftPurchaseOrders();

    /** Bảng 6 – Đặt bàn chờ xác nhận (status = PENDING). */
    List<DashboardPendingBookingItemDTO> getPendingBookings();
}
