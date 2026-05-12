package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.response.dashboard.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlDashboardRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai tất cả các Native SQL query phục vụ Dashboard.
 *
 * <p>Quy ước query:
 * <ul>
 *   <li>Range ngày dùng {@code created_at >= CURDATE() AND created_at < CURDATE() + INTERVAL 1 DAY}
 *       thay vì {@code DATE(created_at) = CURDATE()} để tận dụng index.</li>
 *   <li>So sánh {@code payment_status} dùng {@code UPPER()} để chịu được dữ liệu mixed-case.</li>
 *   <li>Trạng thái đơn hàng so sánh qua bảng {@code dish_order_status} theo {@code dish_order_status_code}.</li>
 * </ul>
 */
@Repository
public class NativeSqlDashboardRepositoryImpl implements NativeSqlDashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // =========================================================================
    // KPI QUERIES
    // =========================================================================

    @Override
    public BigDecimal getRevenueToday() {
        String sql = """
                SELECT COALESCE(SUM(total_amount), 0) AS revenue
                FROM invoice
                WHERE UPPER(payment_status) = 'PAID'
                  AND created_at >= CURDATE()
                  AND created_at < CURDATE() + INTERVAL 1 DAY
                  AND is_active = 1
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }

    @Override
    public int[] getOrderCountsToday() {
        String sql = """
                SELECT
                    COUNT(*) AS total_orders,
                    SUM(CASE WHEN dos.dish_order_status_code = 'PAID' THEN 1 ELSE 0 END) AS paid_orders
                FROM dish_order do
                JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                WHERE do.created_at >= CURDATE()
                  AND do.created_at < CURDATE() + INTERVAL 1 DAY
                  AND do.is_active = 1
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> rows = query.getResultList();
        if (rows.isEmpty()) return new int[]{0, 0};
        Tuple t = rows.get(0);
        int total = NativeSqlTupleUtils.getInteger(t, "total_orders") != null
                ? NativeSqlTupleUtils.getInteger(t, "total_orders") : 0;
        int paid  = NativeSqlTupleUtils.getInteger(t, "paid_orders") != null
                ? NativeSqlTupleUtils.getInteger(t, "paid_orders") : 0;
        return new int[]{total, paid};
    }

    @Override
    public int getOccupiedTableCount() {
        String sql = """
                SELECT COUNT(*) FROM cafe_table
                WHERE table_status = 'OCCUPIED' AND is_active = 1
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    @Override
    public int getActiveStaffCount() {
        String sql = """
                SELECT COUNT(*) FROM account a
                JOIN role r ON a.role_id = r.id
                WHERE a.is_active = 1
                  AND r.role_code IN ('PC-006', 'PC-008')
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    @Override
    public int getProcessingOrderCount() {
        String sql = """
                SELECT COUNT(*) FROM dish_order do
                JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                WHERE dos.dish_order_status_code = 'PROCESSING'
                  AND do.created_at >= CURDATE()
                  AND do.created_at < CURDATE() + INTERVAL 1 DAY
                  AND do.is_active = 1
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    @Override
    public BigDecimal getTotalSupplierDebt() {
        String sql = """
                SELECT COALESCE(SUM(total_amount), 0) AS total_debt
                FROM debt WHERE is_paid = 0
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }

    @Override
    public int getExpiringSoonStockCount() {
        String sql = """
                SELECT COUNT(*) FROM stock_level
                WHERE is_active = 1
                  AND expiration_at >= NOW()
                  AND expiration_at <= NOW() + INTERVAL 30 DAY
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    @Override
    public int getUpcomingBookingCount() {
        String sql = """
                SELECT COUNT(*) FROM table_booking
                WHERE expected_arrive_time BETWEEN NOW() AND NOW() + INTERVAL 2 HOUR
                  AND booking_status NOT IN ('CANCELLED', 'EXPIRED')
                  AND is_active = 1
                """;
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    // =========================================================================
    // CHART QUERIES
    // =========================================================================

    @Override
    public List<DashboardRevenueChartItemDTO> getRevenueLast7Days() {
        String sql = """
                SELECT
                    DATE(created_at) AS date,
                    SUM(total_amount) AS dailyRevenue
                FROM invoice
                WHERE UPPER(payment_status) = 'PAID'
                  AND created_at >= CURDATE() - INTERVAL 6 DAY
                  AND created_at < CURDATE() + INTERVAL 1 DAY
                  AND is_active = 1
                GROUP BY DATE(created_at)
                ORDER BY date
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardRevenueChartItemDTO.builder()
                        .date(NativeSqlTupleUtils.getString(t, "date"))
                        .dailyRevenue(NativeSqlTupleUtils.getBigDecimal(t, "dailyRevenue"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardOrderByHourItemDTO> getOrdersByHourToday() {
        String sql = """
                SELECT
                    HOUR(created_at) AS hour,
                    COUNT(*) AS orderCount
                FROM dish_order
                WHERE created_at >= CURDATE()
                  AND created_at < CURDATE() + INTERVAL 1 DAY
                  AND is_active = 1
                GROUP BY HOUR(created_at)
                ORDER BY hour
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardOrderByHourItemDTO.builder()
                        .hour(NativeSqlTupleUtils.getInteger(t, "hour"))
                        .orderCount(NativeSqlTupleUtils.getInteger(t, "orderCount"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardOrderStatusChartItemDTO> getOrderStatusDistributionToday() {
        String sql = """
                SELECT
                    dos.dish_order_status_name AS status,
                    COUNT(do.id) AS orderCount
                FROM dish_order do
                JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                WHERE do.created_at >= CURDATE()
                  AND do.created_at < CURDATE() + INTERVAL 1 DAY
                  AND do.is_active = 1
                GROUP BY dos.id, dos.dish_order_status_name
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardOrderStatusChartItemDTO.builder()
                        .status(NativeSqlTupleUtils.getString(t, "status"))
                        .orderCount(NativeSqlTupleUtils.getInteger(t, "orderCount"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardTopDishItemDTO> getTop5DishesToday() {
        String sql = """
                SELECT
                    d.dish_name AS dishName,
                    SUM(dod.quantity) AS totalQuantity
                FROM dish_order_detail dod
                JOIN dish_order do ON dod.dish_order_id = do.id
                JOIN dish d ON dod.dish_id = d.id
                JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                WHERE do.created_at >= CURDATE()
                  AND do.created_at < CURDATE() + INTERVAL 1 DAY
                  AND do.is_active = 1
                  AND dos.dish_order_status_code != 'CANCEL'
                GROUP BY d.id, d.dish_name
                ORDER BY totalQuantity DESC
                LIMIT 5
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardTopDishItemDTO.builder()
                        .dishName(NativeSqlTupleUtils.getString(t, "dishName"))
                        .totalQuantity(NativeSqlTupleUtils.getInteger(t, "totalQuantity"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardTableByFloorItemDTO> getTableStatusByFloor() {
        String sql = """
                SELECT
                    floor,
                    SUM(CASE WHEN table_status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied,
                    SUM(CASE WHEN table_status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available
                FROM cafe_table
                WHERE is_active = 1
                GROUP BY floor
                ORDER BY floor
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardTableByFloorItemDTO.builder()
                        .floor(NativeSqlTupleUtils.getInteger(t, "floor"))
                        .occupied(NativeSqlTupleUtils.getInteger(t, "occupied"))
                        .available(NativeSqlTupleUtils.getInteger(t, "available"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardDebtChartItemDTO> getDebtBySupplier() {
        String sql = """
                SELECT
                    s.supplier_name AS supplierName,
                    SUM(d.total_amount) AS debtAmount
                FROM debt d
                JOIN supplier s ON d.supplier_id = s.id
                WHERE d.is_paid = 0
                GROUP BY s.id, s.supplier_name
                ORDER BY debtAmount DESC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardDebtChartItemDTO.builder()
                        .supplierName(NativeSqlTupleUtils.getString(t, "supplierName"))
                        .debtAmount(NativeSqlTupleUtils.getBigDecimal(t, "debtAmount"))
                        .build())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // QUICK-LIST QUERIES
    // =========================================================================

    @Override
    public List<DashboardProcessingOrderItemDTO> getProcessingOrdersToday() {
        String sql = """
                SELECT
                    do.id AS orderId,
                    ct.table_name AS tableName,
                    do.created_at AS createdAt,
                    GROUP_CONCAT(CONCAT(d.dish_name, ' x', dod.quantity) SEPARATOR ', ') AS itemsSummary
                FROM dish_order do
                JOIN cafe_table ct ON do.dining_table_id = ct.id
                JOIN dish_order_detail dod ON do.id = dod.dish_order_id
                JOIN dish d ON dod.dish_id = d.id
                JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                WHERE dos.dish_order_status_code = 'PROCESSING'
                  AND do.created_at >= CURDATE()
                  AND do.created_at < CURDATE() + INTERVAL 1 DAY
                  AND do.is_active = 1
                GROUP BY do.id, ct.table_name, do.created_at
                ORDER BY do.created_at DESC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardProcessingOrderItemDTO.builder()
                        .orderId(NativeSqlTupleUtils.getInteger(t, "orderId"))
                        .tableName(NativeSqlTupleUtils.getString(t, "tableName"))
                        .createdAt(NativeSqlTupleUtils.getInstant(t, "createdAt"))
                        .itemsSummary(NativeSqlTupleUtils.getString(t, "itemsSummary"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardUpcomingBookingItemDTO> getUpcomingBookings() {
        String sql = """
                SELECT
                    tb.expected_arrive_time AS expectedArriveTime,
                    tb.customer_name AS customerName,
                    tb.phone_number AS phoneNumber,
                    ct.table_name AS tableName,
                    tb.note AS note
                FROM table_booking tb
                JOIN cafe_table ct ON tb.dining_table_id = ct.id
                WHERE tb.expected_arrive_time BETWEEN NOW() AND NOW() + INTERVAL 2 HOUR
                  AND tb.booking_status NOT IN ('CANCELLED', 'EXPIRED')
                  AND tb.is_active = 1
                ORDER BY tb.expected_arrive_time
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardUpcomingBookingItemDTO.builder()
                        .expectedArriveTime(NativeSqlTupleUtils.getInstant(t, "expectedArriveTime"))
                        .customerName(NativeSqlTupleUtils.getString(t, "customerName"))
                        .phoneNumber(NativeSqlTupleUtils.getString(t, "phoneNumber"))
                        .tableName(NativeSqlTupleUtils.getString(t, "tableName"))
                        .note(NativeSqlTupleUtils.getString(t, "note"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardStockAlertItemDTO> getStockAlerts() {
        String sql = """
                SELECT
                    i.ingredient_name AS ingredientName,
                    sl.id AS batchId,
                    sl.quantity AS quantity,
                    sl.expiration_at AS expirationAt,
                    w.warehouse_name AS warehouseName
                FROM stock_level sl
                JOIN ingredient i ON sl.ingredient_id = i.id
                JOIN warehouse w ON sl.warehouse_id = w.id
                WHERE sl.is_active = 1
                  AND sl.expiration_at >= NOW()
                  AND (sl.expiration_at <= NOW() + INTERVAL 7 DAY OR sl.quantity <= 5)
                ORDER BY sl.expiration_at ASC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardStockAlertItemDTO.builder()
                        .ingredientName(NativeSqlTupleUtils.getString(t, "ingredientName"))
                        .batchId(NativeSqlTupleUtils.getInteger(t, "batchId"))
                        .quantity(NativeSqlTupleUtils.getBigDecimal(t, "quantity"))
                        .expirationAt(NativeSqlTupleUtils.getInstant(t, "expirationAt"))
                        .warehouseName(NativeSqlTupleUtils.getString(t, "warehouseName"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardPendingInvoiceItemDTO> getPendingInvoices() {
        String sql = """
                SELECT
                    inv.invoice_code AS invoiceCode,
                    ct.table_name AS tableName,
                    inv.total_amount AS totalAmount,
                    inv.created_at AS createdAt,
                    inv.customer_name AS customerName
                FROM invoice inv
                JOIN cafe_table ct ON inv.dining_table_id = ct.id
                WHERE UPPER(inv.payment_status) NOT IN ('PAID', 'ĐÃ HỦY')
                  AND inv.is_active = 1
                ORDER BY inv.created_at ASC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardPendingInvoiceItemDTO.builder()
                        .invoiceCode(NativeSqlTupleUtils.getString(t, "invoiceCode"))
                        .tableName(NativeSqlTupleUtils.getString(t, "tableName"))
                        .totalAmount(NativeSqlTupleUtils.getBigDecimal(t, "totalAmount"))
                        .createdAt(NativeSqlTupleUtils.getInstant(t, "createdAt"))
                        .customerName(NativeSqlTupleUtils.getString(t, "customerName"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardDraftPurchaseOrderItemDTO> getDraftPurchaseOrders() {
        String sql = """
                SELECT
                    po.purchase_order_code AS purchaseOrderCode,
                    po.total_amount AS totalAmount,
                    po.created_at AS createdAt,
                    s.supplier_name AS supplierName
                FROM purchase_order po
                JOIN supplier s ON po.supplier_id = s.id
                WHERE UPPER(po.payment_status) = 'DRAFT'
                  AND po.is_active = 1
                ORDER BY po.created_at DESC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardDraftPurchaseOrderItemDTO.builder()
                        .purchaseOrderCode(NativeSqlTupleUtils.getString(t, "purchaseOrderCode"))
                        .totalAmount(NativeSqlTupleUtils.getBigDecimal(t, "totalAmount"))
                        .createdAt(NativeSqlTupleUtils.getInstant(t, "createdAt"))
                        .supplierName(NativeSqlTupleUtils.getString(t, "supplierName"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardPendingBookingItemDTO> getPendingBookings() {
        String sql = """
                SELECT
                    tb.id            AS bookingId,
                    tb.customer_name AS customerName,
                    tb.phone_number  AS phoneNumber,
                    ct.table_name    AS tableName,
                    tb.expected_arrive_time AS expectedArriveTime,
                    tb.created_at    AS createdAt,
                    tb.note          AS note
                FROM table_booking tb
                JOIN cafe_table ct ON tb.dining_table_id = ct.id
                WHERE tb.booking_status = 'PENDING'
                  AND tb.is_active = 1
                ORDER BY tb.created_at ASC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(t -> DashboardPendingBookingItemDTO.builder()
                        .bookingId(NativeSqlTupleUtils.getInteger(t, "bookingId"))
                        .customerName(NativeSqlTupleUtils.getString(t, "customerName"))
                        .phoneNumber(NativeSqlTupleUtils.getString(t, "phoneNumber"))
                        .tableName(NativeSqlTupleUtils.getString(t, "tableName"))
                        .expectedArriveTime(NativeSqlTupleUtils.getInstant(t, "expectedArriveTime"))
                        .createdAt(NativeSqlTupleUtils.getInstant(t, "createdAt"))
                        .note(NativeSqlTupleUtils.getString(t, "note"))
                        .build())
                .collect(Collectors.toList());
    }
}
