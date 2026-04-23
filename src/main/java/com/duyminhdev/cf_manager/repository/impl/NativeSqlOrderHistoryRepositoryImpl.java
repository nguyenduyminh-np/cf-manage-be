package com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.OrderHistoryNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.OrderHistorySearchRequestDTO;
import com.duyminhdev.cf_manager.repository.NativeSqlOrderHistoryRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import com.duyminhdev.cf_manager.utils.PageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NativeSqlOrderHistoryRepositoryImpl implements NativeSqlOrderHistoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
            SELECT
                do.id AS dishOrderId,
                dt.table_name AS tableName,
                acc.full_name AS employeeName,
                dos.dish_order_status_name AS orderStatus,
                dos.dish_order_status_code AS dishOrderStatusCode,
                do.created_at AS createdAt,
                do.note AS note,
                COALESCE(SUM(dod.quantity), 0) AS totalQuantity,
                COALESCE(SUM(dod.quantity * d.price), 0) AS totalAmount
            """;

    private static final String FROM_WHERE = """
            FROM dish_order do
            INNER JOIN dining_table dt ON do.dining_table_id = dt.id
            INNER JOIN account acc ON do.account_id = acc.id
            INNER JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
            LEFT JOIN dish_order_detail dod ON do.id = dod.dish_order_id
            LEFT JOIN dish d ON dod.dish_id = d.id
            WHERE 1 = 1
            """;

    private static final String GROUP_BY = """
            GROUP BY
                dt.table_name,
                acc.full_name,
                dos.dish_order_status_name,
                dos.dish_order_status_code,
                do.created_at,
                do.note,
                do.id
            """;

    @Override
    public PageResponse<List<OrderHistoryNativeResultDTO>> search(OrderHistorySearchRequestDTO request) {
        OrderHistorySearchRequestDTO safeRequest = request != null ? request : new OrderHistorySearchRequestDTO();

        int pageNo = PageUtils.normalizePage(safeRequest.getPage());
        int pageSize = PageUtils.normalizeLimit(safeRequest.getLimit());
        int offset = pageNo * pageSize;

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(safeRequest, params);
        String orderBy = buildOrderBy(safeRequest.getSortField(), safeRequest.getSortDir());

        String dataSql = SELECT_COLUMNS
                + FROM_WHERE
                + whereClause
                + GROUP_BY
                + orderBy
                + " LIMIT " + offset + ", " + pageSize;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();

        List<OrderHistoryNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .toList();

        long totalElements = countTotalElements(safeRequest);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<OrderHistoryNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    private long countTotalElements(OrderHistorySearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();

        // Đếm số lượng đơn hàng duy nhất (do.id) thỏa điều kiện
        String countSql = """
                SELECT COUNT(DISTINCT do.id)
                FROM dish_order do
                INNER JOIN dining_table dt ON do.dining_table_id = dt.id
                INNER JOIN account acc ON do.account_id = acc.id
                INNER JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
                LEFT JOIN dish_order_detail dod ON do.id = dod.dish_order_id
                LEFT JOIN dish d ON dod.dish_id = d.id
                WHERE 1 = 1
                """ + buildWhereClause(request, params);

        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);

        Object result = countQuery.getSingleResult();
        return ((Number) result).longValue();
    }

    private String buildWhereClause(OrderHistorySearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) {
            return sql.toString();
        }

        if (request.getDiningTableId() != null) {
            sql.append(" AND do.dining_table_id = :diningTableId ");
            params.put("diningTableId", request.getDiningTableId());
        }

        if (StringUtils.hasText(request.getTableName())) {
            sql.append(" AND LOWER(dt.table_name) LIKE :tableNameKeyword ESCAPE '\\\\' ");
            String keyword = "%" + NativeSqlTupleUtils.escapeLike(request.getTableName().trim().toLowerCase()) + "%";
            params.put("tableNameKeyword", keyword);
        }

        if (StringUtils.hasText(request.getEmployeeName())) {
            sql.append(" AND LOWER(acc.full_name) LIKE :employeeNameKeyword ESCAPE '\\\\' ");
            String keyword = "%" + NativeSqlTupleUtils.escapeLike(request.getEmployeeName().trim().toLowerCase()) + "%";
            params.put("employeeNameKeyword", keyword);
        }

        if (StringUtils.hasText(request.getStatus())) {
            String normalizedStatus = request.getStatus().trim().toLowerCase();

            sql.append(" AND ( ")
                    .append("LOWER(dos.dish_order_status_code) = :statusCode ")
                    .append("OR LOWER(dos.dish_order_status_name) LIKE :statusKeyword ESCAPE '\\\\' ")
                    .append(") ");

            params.put("statusCode", normalizedStatus);
            params.put("statusKeyword", "%" + NativeSqlTupleUtils.escapeLike(normalizedStatus) + "%");
        }

        return sql.toString();
    }

    private void bindParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String dbColumn = mapSortFieldToDbColumn(sortField);

        if (!StringUtils.hasText(dbColumn)) {
            return " ORDER BY do.created_at DESC, do.id DESC ";
        }

        String direction = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return " ORDER BY " + dbColumn + " " + direction + ", do.id DESC ";
    }

    private String mapSortFieldToDbColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return null;
        }

        return switch (sortField.trim()) {
            case "tableName"      -> "dt.table_name";
            case "employeeName"   -> "acc.full_name";
            case "orderStatus"    -> "dos.dish_order_status_name";
            case "createdAt"      -> "do.created_at";
            case "totalQuantity"  -> "totalQuantity";
            case "totalAmount"    -> "totalAmount";
            default               -> null;
        };
    }

    private OrderHistoryNativeResultDTO mapTupleToDto(Tuple tuple) {
        return OrderHistoryNativeResultDTO.builder()
                .dishOrderId(NativeSqlTupleUtils.getInteger(tuple, "dishOrderId"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .employeeName(NativeSqlTupleUtils.getString(tuple, "employeeName"))
                .orderStatus(NativeSqlTupleUtils.getString(tuple, "orderStatus"))
                .dishOrderStatusCode(NativeSqlTupleUtils.getString(tuple, "dishOrderStatusCode"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .note(NativeSqlTupleUtils.getString(tuple, "note"))
                .totalQuantity(NativeSqlTupleUtils.getLong(tuple, "totalQuantity"))
                .totalAmount(NativeSqlTupleUtils.getBigDecimal(tuple, "totalAmount"))
                .build();
    }
}