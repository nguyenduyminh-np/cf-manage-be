package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.WarehouseSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.warehouse.WarehouseSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlWarehouseRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import com.duyminhdev.cf_manager.utils.PageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class NativeSqlWarehouseRepositoryImpl implements NativeSqlWarehouseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
        SELECT
            w.id AS id,
            w.warehouse_code AS warehouseCode,
            w.warehouse_name AS warehouseName,
            w.location AS location,
            w.note AS note,
            w.created_at AS createdTime,
            w.is_active AS active,
            (SELECT COUNT(DISTINCT sl.ingredient_id)
             FROM stock_level sl
             WHERE sl.warehouse_id = w.id
               AND sl.is_active = 1
               AND sl.quantity > 0) AS ingredientCount
    """;

    private static final String FROM = "FROM warehouse w";

    @Override
    public PageResponse<List<WarehouseSearchNativeResultDTO>> search(WarehouseSearchRequestDTO request) {
        request = request != null ? request : new WarehouseSearchRequestDTO();
        int pageNo = PageUtils.normalizePage(request.getPage());
        int pageSize = PageUtils.normalizeLimit(request.getLimit());
        int offset = pageNo * pageSize;

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String orderBy = buildOrderBy(request.getSortField(), request.getSortDir());

        String dataSql = SELECT_COLUMNS + " " + FROM + " " + whereClause + " " + orderBy
                + " LIMIT " + offset + ", " + pageSize;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();
        List<WarehouseSearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .collect(Collectors.toList());

        long totalElements = countTotalElements(request);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<WarehouseSearchNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    @Override
    public List<WarehouseSearchNativeResultDTO> findAllByCriteria(WarehouseSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String orderBy = buildOrderBy(request.getSortField(), request.getSortDir());

        String sql = SELECT_COLUMNS + " " + FROM + " " + whereClause + " " + orderBy;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        bindParameters(query, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream().map(this::mapTupleToDto).collect(Collectors.toList());
    }

    private long countTotalElements(WarehouseSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String countSql = "SELECT COUNT(1) FROM warehouse w " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private String buildWhereClause(WarehouseSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("WHERE 1=1 ");

        if (request.getActive() != null) {
            sql.append(" AND w.is_active = :active ");
            params.put("active", request.getActive() ? 1 : 0);
        }

        if (StringUtils.hasText(request.getSearchString())) {
            sql.append(" AND (LOWER(w.warehouse_code) LIKE :keyword ESCAPE '\\\\' "
                     + "OR LOWER(w.warehouse_name) LIKE :keyword ESCAPE '\\\\') ");
            String keyword = "%" + NativeSqlTupleUtils.escapeLike(
                    request.getSearchString().trim().toLowerCase()) + "%";
            params.put("keyword", keyword);
        }

        return sql.toString();
    }

    private void bindParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String col = switch (StringUtils.hasText(sortField) ? sortField.trim() : "") {
            case "id"               -> "w.id";
            case "warehouseCode"    -> "w.warehouse_code";
            case "warehouseName"    -> "w.warehouse_name";
            case "location"         -> "w.location";
            case "createdTime"      -> "w.created_at";
            case "active"           -> "w.is_active";
            case "ingredientCount"  -> "ingredientCount";
            default                 -> null;
        };

        if (col == null) {
            return "ORDER BY w.warehouse_name ASC, w.id ASC";
        }

        String direction = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return "ORDER BY " + col + " " + direction + ", w.id ASC";
    }

    private WarehouseSearchNativeResultDTO mapTupleToDto(Tuple tuple) {
        return WarehouseSearchNativeResultDTO.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .warehouseCode(NativeSqlTupleUtils.getString(tuple, "warehouseCode"))
                .warehouseName(NativeSqlTupleUtils.getString(tuple, "warehouseName"))
                .location(NativeSqlTupleUtils.getString(tuple, "location"))
                .note(NativeSqlTupleUtils.getString(tuple, "note"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .ingredientCount(NativeSqlTupleUtils.getInteger(tuple, "ingredientCount"))
                .build();
    }
}
