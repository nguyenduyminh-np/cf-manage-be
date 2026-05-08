package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.*;
import com.duyminhdev.cf_manager.dto.request.purchase_order.PurchaseOrderSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlPurchaseOrderRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import java.util.*;

@Repository
public class NativeSqlPurchaseOrderRepositoryImpl implements NativeSqlPurchaseOrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PurchaseOrderSearchNativeResult> search(PurchaseOrderSearchRequestDTO request, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    po.id              AS id,
                    po.purchase_order_code AS purchaseOrderCode,
                    po.total_amount    AS totalPrice,
                    po.payment_status  AS paymentStatus,
                    po.ordered_at      AS orderDate,
                    po.created_at      AS createdTime,
                    a.full_name        AS fullName,
                    s.supplier_name    AS supplierName
                FROM purchase_order po
                INNER JOIN account a  ON a.id  = po.account_id
                LEFT  JOIN supplier s ON s.id  = po.supplier_id
                WHERE po.is_active = 1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        // sorting
        String sortField = StringUtils.hasText(request.getSortField()) ? request.getSortField() : "createdTime";
        String sortDir = "desc".equalsIgnoreCase(request.getSortDir()) ? "DESC" : "ASC";
        switch (sortField) {
            case "totalPrice":
                sql.append(" ORDER BY po.total_amount ").append(sortDir); break;
            case "paymentStatus":
                sql.append(" ORDER BY po.payment_status ").append(sortDir); break;
            case "createdTime":
            default:
                sql.append(" ORDER BY po.created_at ").append(sortDir); break;
        }

        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);

        Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream().map(this::mapSearchRow).toList();
    }

    @Override
    public long count(PurchaseOrderSearchRequestDTO request) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM purchase_order po
                INNER JOIN account a  ON a.id  = po.account_id
                LEFT  JOIN supplier s ON s.id  = po.supplier_id
                WHERE po.is_active = 1
                """);
        Map<String, Object> params = buildSearchParams(sql, request);
        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Map<String, Object> buildSearchParams(StringBuilder sql, PurchaseOrderSearchRequestDTO request) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getPurchaseOrderCode())) {
            sql.append(" AND po.purchase_order_code LIKE :purchaseOrderCode");
            params.put("purchaseOrderCode", "%" + request.getPurchaseOrderCode().trim() + "%");
        }
        if (StringUtils.hasText(request.getPaymentStatus())) {
            sql.append(" AND po.payment_status = :paymentStatus");
            params.put("paymentStatus", request.getPaymentStatus().trim());
        }
        if (request.getTotalPriceFrom() != null) {
            sql.append(" AND po.total_amount >= :totalPriceFrom");
            params.put("totalPriceFrom", request.getTotalPriceFrom());
        }
        if (request.getTotalPriceTo() != null) {
            sql.append(" AND po.total_amount <= :totalPriceTo");
            params.put("totalPriceTo", request.getTotalPriceTo());
        }
        if (request.getFromDate() != null) {
            sql.append(" AND po.created_at >= :fromDate");
            params.put("fromDate", request.getFromDate());
        }
        if (request.getToDate() != null) {
            sql.append(" AND po.created_at <= :toDate");
            params.put("toDate", request.getToDate());
        }
        return params;
    }

    private PurchaseOrderSearchNativeResult mapSearchRow(Tuple tuple) {
        return PurchaseOrderSearchNativeResult.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .purchaseOrderCode(NativeSqlTupleUtils.getString(tuple, "purchaseOrderCode"))
                .totalPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "totalPrice"))
                .paymentStatus(NativeSqlTupleUtils.getString(tuple, "paymentStatus"))
                .orderDate(NativeSqlTupleUtils.getInstant(tuple, "orderDate"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .fullName(NativeSqlTupleUtils.getString(tuple, "fullName"))
                .supplierName(NativeSqlTupleUtils.getString(tuple, "supplierName"))
                .build();
    }

    @Override
    public Optional<PurchaseOrderDetailNativeResult> findDetailById(Integer id) {
        String sql = """
                SELECT
                    po.id              AS id,
                    po.purchase_order_code AS purchaseOrderCode,
                    po.total_amount    AS totalPrice,
                    po.payment_status  AS paymentStatus,
                    po.ordered_at      AS orderDate,
                    po.created_at      AS createdTime,
                    a.id               AS accountId,
                    a.full_name        AS fullName,
                    s.id               AS supplierId,
                    s.supplier_name    AS supplierName,
                    w.id               AS warehouseId,
                    w.warehouse_name   AS warehouseName
                FROM purchase_order po
                INNER JOIN account a   ON a.id  = po.account_id
                LEFT  JOIN supplier s  ON s.id  = po.supplier_id
                LEFT  JOIN warehouse w ON w.id  = po.warehouse_id
                WHERE po.id = :id AND po.is_active = 1
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("id", id);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        if (tuples.isEmpty()) return Optional.empty();
        Tuple tuple = tuples.get(0);
        return Optional.of(PurchaseOrderDetailNativeResult.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .purchaseOrderCode(NativeSqlTupleUtils.getString(tuple, "purchaseOrderCode"))
                .totalPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "totalPrice"))
                .paymentStatus(NativeSqlTupleUtils.getString(tuple, "paymentStatus"))
                .orderDate(NativeSqlTupleUtils.getInstant(tuple, "orderDate"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .accountId(NativeSqlTupleUtils.getInteger(tuple, "accountId"))
                .fullName(NativeSqlTupleUtils.getString(tuple, "fullName"))
                .supplierId(NativeSqlTupleUtils.getInteger(tuple, "supplierId"))
                .supplierName(NativeSqlTupleUtils.getString(tuple, "supplierName"))
                .warehouseId(NativeSqlTupleUtils.getInteger(tuple, "warehouseId"))
                .warehouseName(NativeSqlTupleUtils.getString(tuple, "warehouseName"))
                .build());
    }

    @Override
    public List<PurchaseOrderItemNativeResult> findItemsByOrderId(Integer orderId) {
        String sql = """
                SELECT
                    pod.id             AS detailId,
                    i.id               AS ingredientId,
                    i.ingredient_code  AS ingredientCode,
                    i.ingredient_name  AS ingredientName,
                    i.supplier_id      AS supplierId,
                    pod.quantity       AS quantity,
                    pod.unit_price     AS unitPrice
                FROM purchase_order_detail pod
                INNER JOIN ingredient i ON i.id = pod.ingredient_id
                WHERE pod.purchase_order_id = :orderId AND pod.is_active = 1
                ORDER BY pod.id ASC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("orderId", orderId);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream().map(tuple -> PurchaseOrderItemNativeResult.builder()
                .detailId(NativeSqlTupleUtils.getInteger(tuple, "detailId"))
                .ingredientId(NativeSqlTupleUtils.getInteger(tuple, "ingredientId"))
                .ingredientCode(NativeSqlTupleUtils.getString(tuple, "ingredientCode"))
                .ingredientName(NativeSqlTupleUtils.getString(tuple, "ingredientName"))
                .supplierId(NativeSqlTupleUtils.getInteger(tuple, "supplierId"))
                .quantity(NativeSqlTupleUtils.getInteger(tuple, "quantity"))
                .unitPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "unitPrice"))
                .build()).toList();
    }

    @Override
    public List<PurchaseOrderIngredientSelectNativeResult> findIngredientsBySupplierId(Integer supplierId) {
        String sql = """
                SELECT
                    i.id               AS ingredientId,
                    i.ingredient_code  AS ingredientCode,
                    i.ingredient_name  AS ingredientName,
                    i.supplier_id      AS supplierId
                FROM ingredient i
                INNER JOIN supplier s ON s.id = i.supplier_id
                WHERE i.is_active = 1
                  AND s.is_active = 1
                  AND s.id = :supplierId
                ORDER BY i.ingredient_name ASC, i.id ASC
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("supplierId", supplierId);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
                .map(tuple -> PurchaseOrderIngredientSelectNativeResult.builder()
                        .ingredientId(NativeSqlTupleUtils.getInteger(tuple, "ingredientId"))
                        .ingredientCode(NativeSqlTupleUtils.getString(tuple, "ingredientCode"))
                        .ingredientName(NativeSqlTupleUtils.getString(tuple, "ingredientName"))
                        .supplierId(NativeSqlTupleUtils.getInteger(tuple, "supplierId"))
                        .build())
                .toList();
    }
}
