package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientStockDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.SalesSummaryDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailabilityDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlChatRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NativeSqlChatRepositoryImpl implements NativeSqlChatRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // ============================================================
    // 1. Bàn trống theo tầng và khung giờ
    // ============================================================
    @Override
    public List<TableAvailabilityDTO> findAvailableTables(Integer floor, Instant start, Instant end) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    dt.id          AS tableId,
                    dt.table_code  AS tableCode,
                    dt.table_name  AS tableName,
                    dt.floor       AS floor,
                    dt.slot        AS slot
                FROM cafe_table dt
                WHERE dt.is_active = 1
                  AND dt.id NOT IN (
                      SELECT tb.dining_table_id
                      FROM table_booking tb
                      WHERE tb.is_active = 1
                        AND tb.booking_status NOT IN ('CANCELLED', 'CHECKED_OUT', 'NO_SHOW')
                        AND tb.expected_arrive_time  < :endTime
                        AND tb.expected_check_out    > :startTime
                  )
                """);

        if (floor != null) {
            sql.append(" AND dt.floor = :floor");
        }
        sql.append(" ORDER BY dt.floor ASC, dt.slot DESC");

        Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
        query.setParameter("startTime", start);
        query.setParameter("endTime", end);
        if (floor != null) {
            query.setParameter("floor", floor);
        }

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        List<TableAvailabilityDTO> result = new ArrayList<>();
        for (Tuple t : tuples) {
            result.add(TableAvailabilityDTO.builder()
                    .tableId(NativeSqlTupleUtils.getInteger(t, "tableId"))
                    .tableCode(NativeSqlTupleUtils.getString(t, "tableCode"))
                    .tableName(NativeSqlTupleUtils.getString(t, "tableName"))
                    .floor(NativeSqlTupleUtils.getInteger(t, "floor"))
                    .slot(NativeSqlTupleUtils.getInteger(t, "slot"))
                    .build());
        }
        return result;
    }

    // ============================================================
    // 2. Tổng hợp doanh thu
    // ============================================================
    @Override
    public SalesSummaryDTO getSalesSummary(Instant from, Instant to) {
        String sql = """
                SELECT
                    COALESCE(SUM(i.total_amount), 0) AS totalRevenue,
                    COUNT(i.id)                       AS invoiceCount
                FROM invoice i
                WHERE i.is_active     = 1
                  AND i.payment_status = 'PAID'
                  AND i.created_at    >= :fromTime
                  AND i.created_at    <= :toTime
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("fromTime", from);
        query.setParameter("toTime", to);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        if (tuples.isEmpty()) {
            return SalesSummaryDTO.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .invoiceCount(0L)
                    .build();
        }
        Tuple t = tuples.get(0);
        return SalesSummaryDTO.builder()
                .totalRevenue(NativeSqlTupleUtils.getBigDecimal(t, "totalRevenue"))
                .invoiceCount(NativeSqlTupleUtils.getLong(t, "invoiceCount"))
                .build();
    }

    // ============================================================
    // 3. Nguyên liệu sắp hết / sắp hết hạn
    // ============================================================
    @Override
    public List<IngredientStockDTO> getLowStockIngredients() {
        // Lấy nguyên liệu có tổng tồn kho < 10 HOẶC hết hạn trong 7 ngày tới
        String sql = """
                SELECT
                    ing.ingredient_name              AS ingredientName,
                    COALESCE(SUM(sl.quantity), 0)    AS totalQuantity,
                    u.unit_name                      AS unitName,
                    MIN(sl.expiration_at)            AS nearestExpirationAt,
                    CASE
                        WHEN MIN(sl.expiration_at) <= DATE_ADD(NOW(), INTERVAL 7 DAY) THEN 1
                        ELSE 0
                    END                              AS isNearExpiry
                FROM ingredient ing
                LEFT JOIN stock_level sl ON sl.ingredient_id = ing.id AND sl.is_active = 1
                LEFT JOIN unit u         ON u.id = ing.unit_id
                WHERE ing.is_active = 1
                GROUP BY ing.id, ing.ingredient_name, u.unit_name
                HAVING
                    COALESCE(SUM(sl.quantity), 0) < 10
                    OR MIN(sl.expiration_at) <= DATE_ADD(NOW(), INTERVAL 7 DAY)
                ORDER BY totalQuantity ASC
                LIMIT 20
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        List<IngredientStockDTO> result = new ArrayList<>();
        for (Tuple t : tuples) {
            Integer totalQty = NativeSqlTupleUtils.getInteger(t, "totalQuantity");
            Instant nearestExp = NativeSqlTupleUtils.getInstant(t, "nearestExpirationAt");
            Boolean isNearExpiry = NativeSqlTupleUtils.getBoolean(t, "isNearExpiry");

            result.add(IngredientStockDTO.builder()
                    .ingredientName(NativeSqlTupleUtils.getString(t, "ingredientName"))
                    .totalQuantity(totalQty != null ? totalQty : 0)
                    .unitName(NativeSqlTupleUtils.getString(t, "unitName"))
                    .nearestExpirationAt(nearestExp)
                    .isNearExpiry(Boolean.TRUE.equals(isNearExpiry))
                    .build());
        }
        return result;
    }

    // ============================================================
    // 4. Top món bán chạy
    // ============================================================
    @Override
    public List<Object[]> getTopSellingDishes(Instant from, Instant to, int limit) {
        String sql = """
                SELECT
                    d.dish_name                  AS dishName,
                    COALESCE(SUM(od.quantity), 0) AS totalSold
                FROM dish d
                INNER JOIN dish_order_detail od ON od.dish_id = d.id
                INNER JOIN invoice i            ON i.dish_order_id = od.dish_order_id
                WHERE i.is_active      = 1
                  AND i.payment_status = 'PAID'
                  AND i.created_at    >= :fromTime
                  AND i.created_at    <= :toTime
                GROUP BY d.id, d.dish_name
                ORDER BY totalSold DESC
                LIMIT :lim
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("fromTime", from);
        query.setParameter("toTime", to);
        query.setParameter("lim", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows;
    }
}
