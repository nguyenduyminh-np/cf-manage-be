package com.duyminhdev.cf_manager.repository.impl;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.NativeSqlDishRepository;
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
public class NativeSqlDishRepositoryImpl implements NativeSqlDishRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
            SELECT
                d.id AS id,
                d.dish_code AS dishCode,
                d.dish_name AS dishName,
                d.price AS price,
                d.photo AS photo,
                d.created_at AS createdAt,
                d.dish_category_id AS dishCategoryId,
                dc.dish_category_name AS dishCategoryName
            """;

    private static final String FROM_WHERE = """
            FROM dish d
            INNER JOIN dish_category dc ON d.dish_category_id = dc.id
            WHERE (d.is_active = 1 OR d.is_active = true)
              AND (dc.is_active = 1 OR dc.is_active = true)
            """;

    @Override
    public PageResponse<List<DishSearchNativeResultDTO>> search(DishSearchRequestDTO request) {
        DishSearchRequestDTO safeRequest = request != null ? request : new DishSearchRequestDTO();

        int pageNo = PageUtils.normalizePage(safeRequest.getPage());
        int pageSize = PageUtils.normalizeLimit(safeRequest.getLimit());
        int offset = pageNo * pageSize;

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(safeRequest, params);
        String orderBy = buildOrderBy(safeRequest.getSortField(), safeRequest.getSortDir());

        String dataSql = SELECT_COLUMNS
                + FROM_WHERE
                + whereClause
                + orderBy
                + " LIMIT " + offset + ", " + pageSize;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();

        List<DishSearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .toList();

        long totalElements = countTotalElements(safeRequest);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<DishSearchNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    private long countTotalElements(DishSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();

        String countSql = """
                SELECT COUNT(1)
                FROM dish d
                INNER JOIN dish_category dc ON d.dish_category_id = dc.id
                WHERE (d.is_active = 1 OR d.is_active = true)
                  AND (dc.is_active = 1 OR dc.is_active = true)
                """ + buildWhereClause(request, params);

        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);

        Object result = countQuery.getSingleResult();
        return ((Number) result).longValue();
    }

    private String buildWhereClause(DishSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) {
            return sql.toString();
        }

        // Search linh động trên dish_code và dish_name
        if (StringUtils.hasText(request.getSearchString())) {
            sql.append("""
                     AND (
                        LOWER(d.dish_code) LIKE :searchKeyword ESCAPE '\\\\'
                        OR LOWER(d.dish_name) LIKE :searchKeyword ESCAPE '\\\\'
                     )
                    """);
            String keyword = "%" + NativeSqlTupleUtils.escapeLike(request.getSearchString().trim().toLowerCase()) + "%";
            params.put("searchKeyword", keyword);
        }

        if (request.getDishCategoryId() != null) {
            sql.append(" AND d.dish_category_id = :dishCategoryId ");
            params.put("dishCategoryId", request.getDishCategoryId());
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
            return " ORDER BY d.dish_name ASC, d.id ASC ";
        }

        String direction = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return " ORDER BY " + dbColumn + " " + direction + ", d.id ASC ";
    }

    private String mapSortFieldToDbColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return null;
        }

        return switch (sortField.trim()) {
            case "id"               -> "d.id";
            case "dishCode"         -> "d.dish_code";
            case "dishName"         -> "d.dish_name";
            case "price"            -> "d.price";
            case "createdAt"        -> "d.created_at";
            case "dishCategoryId"   -> "d.dish_category_id";
            case "dishCategoryName" -> "dc.dish_category_name";
            default                 -> null;
        };
    }

    private DishSearchNativeResultDTO mapTupleToDto(Tuple tuple) {
        return DishSearchNativeResultDTO.builder()
                .id(NativeSqlTupleUtils.getLong(tuple, "id"))
                .dishCode(NativeSqlTupleUtils.getString(tuple, "dishCode"))
                .dishName(NativeSqlTupleUtils.getString(tuple, "dishName"))
                .price(NativeSqlTupleUtils.getBigDecimal(tuple, "price"))
                .photo(NativeSqlTupleUtils.getString(tuple, "photo"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .dishCategoryId(NativeSqlTupleUtils.getLong(tuple, "dishCategoryId"))
                .dishCategoryName(NativeSqlTupleUtils.getString(tuple, "dishCategoryName"))
                .build();
    }
}