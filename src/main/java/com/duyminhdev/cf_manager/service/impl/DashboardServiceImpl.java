package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.response.dashboard.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlDashboardRepository;
import com.duyminhdev.cf_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Triển khai {@link DashboardService}.
 * Tất cả logic nghiệp vụ nhẹ (gom KPI) được thực hiện ở đây;
 * các câu lệnh SQL thực tế nằm trong {@link NativeSqlDashboardRepository}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final NativeSqlDashboardRepository dashboardRepository;

    // =========================================================================
    // KPI – Gom 8 scalar query vào 1 DTO
    // =========================================================================

    @Override
    public DashboardKpiDTO getKpi() {
        BigDecimal revenueToday      = dashboardRepository.getRevenueToday();
        int[]      orderCounts       = dashboardRepository.getOrderCountsToday();
        int        occupiedTables    = dashboardRepository.getOccupiedTableCount();
        int        activeStaff       = dashboardRepository.getActiveStaffCount();
        int        processingOrders  = dashboardRepository.getProcessingOrderCount();
        BigDecimal totalDebt         = dashboardRepository.getTotalSupplierDebt();
        int        expiringSoon      = dashboardRepository.getExpiringSoonStockCount();
        int        upcomingBookings  = dashboardRepository.getUpcomingBookingCount();

        return DashboardKpiDTO.builder()
                .revenueToday(revenueToday)
                .totalOrdersToday(orderCounts[0])
                .paidOrdersToday(orderCounts[1])
                .occupiedTables(occupiedTables)
                .activeStaff(activeStaff)
                .processingOrders(processingOrders)
                .totalSupplierDebt(totalDebt)
                .expiringSoonStock(expiringSoon)
                .upcomingBookings(upcomingBookings)
                .build();
    }

    // =========================================================================
    // CHARTS
    // =========================================================================

    @Override
    public List<DashboardRevenueChartItemDTO> getRevenueLast7Days() {
        return dashboardRepository.getRevenueLast7Days();
    }

    @Override
    public List<DashboardOrderByHourItemDTO> getOrdersByHourToday() {
        return dashboardRepository.getOrdersByHourToday();
    }

    @Override
    public List<DashboardOrderStatusChartItemDTO> getOrderStatusDistributionToday() {
        return dashboardRepository.getOrderStatusDistributionToday();
    }

    @Override
    public List<DashboardTopDishItemDTO> getTop5DishesToday() {
        return dashboardRepository.getTop5DishesToday();
    }

    @Override
    public List<DashboardTableByFloorItemDTO> getTableStatusByFloor() {
        return dashboardRepository.getTableStatusByFloor();
    }

    @Override
    public List<DashboardDebtChartItemDTO> getDebtBySupplier() {
        return dashboardRepository.getDebtBySupplier();
    }

    // =========================================================================
    // QUICK LISTS
    // =========================================================================

    @Override
    public List<DashboardProcessingOrderItemDTO> getProcessingOrdersToday() {
        return dashboardRepository.getProcessingOrdersToday();
    }

    @Override
    public List<DashboardUpcomingBookingItemDTO> getUpcomingBookings() {
        return dashboardRepository.getUpcomingBookings();
    }

    @Override
    public List<DashboardStockAlertItemDTO> getStockAlerts() {
        return dashboardRepository.getStockAlerts();
    }

    @Override
    public List<DashboardPendingInvoiceItemDTO> getPendingInvoices() {
        return dashboardRepository.getPendingInvoices();
    }

    @Override
    public List<DashboardDraftPurchaseOrderItemDTO> getDraftPurchaseOrders() {
        return dashboardRepository.getDraftPurchaseOrders();
    }

    @Override
    public List<DashboardPendingBookingItemDTO> getPendingBookings() {
        return dashboardRepository.getPendingBookings();
    }
}
