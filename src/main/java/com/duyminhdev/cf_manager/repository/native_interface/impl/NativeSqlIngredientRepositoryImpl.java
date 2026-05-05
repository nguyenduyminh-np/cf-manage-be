package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient.IngredientSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlIngredientRepository;
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
public class NativeSqlIngredientRepositoryImpl implements NativeSqlIngredientRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
        SELECT
            i.id AS id,
            i.ingredient_code AS ingredientCode,
            i.ingredient_name AS ingredientName,
            i.shelf_life AS selfLife,
            i.average_price AS averagePrice,
            i.created_at AS createdTime,
            i.is_active AS active,
            i.ingredient_category_id AS ingredientCategoryId,
            ic.ingredient_category_name AS ingredientCategoryName,
            i.supplier_id AS supplierId,
            s.supplier_name AS supplierName,
            i.unit_id AS unitId,
            u.unit_name AS unitName,
            COALESCE(
                (SELECT SUM(sl.quantity)
                 FROM stock_level sl
                 WHERE sl.ingredient_id = i.id
                   AND sl.is_active = 1
                   AND sl.expiration_at > CURRENT_TIMESTAMP),
                0
            ) AS currentStock
    """;

    private static final String FROM_JOIN = """
        FROM ingredient i
        INNER JOIN ingredient_category ic ON i.ingredient_category_id = ic.id AND (ic.is_active = 1 OR ic.is_active = true)
        INNER JOIN supplier s ON i.supplier_id = s.id AND (s.is_active = 1 OR s.is_active = true)
        INNER JOIN unit u ON i.unit_id = u.id AND (u.is_active = 1 OR u.is_active = true)
    """;

    @Override
    public PageResponse<List<IngredientSearchNativeResultDTO>> search(IngredientSearchRequestDTO request) {
        IngredientSearchRequestDTO safeRequest = request != null ? request : new IngredientSearchRequestDTO();
        int pageNo = PageUtils.normalizePage(safeRequest.getPage());
        int pageSize = PageUtils.normalizeLimit(safeRequest.getLimit());
        int offset = pageNo * pageSize;

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(safeRequest, params);
        String orderBy = buildOrderBy(safeRequest.getSortField(), safeRequest.getSortDir());

        String dataSql = SELECT_COLUMNS + FROM_JOIN + whereClause + orderBy
                + " LIMIT " + offset + ", " + pageSize;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();
        List<IngredientSearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .collect(Collectors.toList());

        long totalElements = countTotalElements(safeRequest);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<IngredientSearchNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    @Override
    public List<IngredientSearchNativeResultDTO> findAllByCriteria(IngredientSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String orderBy = buildOrderBy(request.getSortField(), request.getSortDir());

        String sql = SELECT_COLUMNS + FROM_JOIN + whereClause + orderBy;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        bindParameters(query, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream().map(this::mapTupleToDto).collect(Collectors.toList());
    }

    private long countTotalElements(IngredientSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);

        String countSql = "SELECT COUNT(1) FROM ingredient i"
                + " INNER JOIN ingredient_category ic ON i.ingredient_category_id = ic.id AND (ic.is_active = 1 OR ic.is_active = true)"
                + " INNER JOIN supplier s ON i.supplier_id = s.id AND (s.is_active = 1 OR s.is_active = true)"
                + " INNER JOIN unit u ON i.unit_id = u.id AND (u.is_active = 1 OR u.is_active = true)"
                + whereClause;

        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private String buildWhereClause(IngredientSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) return sql.toString();

        // Lọc active
        if (request.getActive() != null) {
            sql.append(" AND i.is_active = :active ");
            params.put("active", request.getActive());
        }

        // Tìm kiếm theo mã hoặc tên
        if (StringUtils.hasText(request.getSearchString())) {
            sql.append(" AND (LOWER(i.ingredient_code) LIKE :searchKeyword ESCAPE '\\\\'"
                    + " OR LOWER(i.ingredient_name) LIKE :searchKeyword ESCAPE '\\\\') ");
            String keyword = "%" + NativeSqlTupleUtils.escapeLike(
                    request.getSearchString().trim().toLowerCase()) + "%";
            params.put("searchKeyword", keyword);
        }

        if (request.getIngredientCategoryId() != null) {
            sql.append(" AND i.ingredient_category_id = :catId ");
            params.put("catId", request.getIngredientCategoryId());
        }

        if (request.getSupplierId() != null) {
            sql.append(" AND i.supplier_id = :supId ");
            params.put("supId", request.getSupplierId());
        }

        return sql.toString();
    }

    private void bindParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String col = switch (StringUtils.hasText(sortField) ? sortField.trim() : "") {
            case "id" -> "i.id";
            case "ingredientCode" -> "i.ingredient_code";
            case "ingredientName" -> "i.ingredient_name";
            case "selfLife" -> "i.shelf_life";
            case "averagePrice" -> "i.average_price";
            case "createdTime" -> "i.created_at";
            case "active" -> "i.is_active";
            case "ingredientCategoryName" -> "ic.ingredient_category_name";
            case "supplierName" -> "s.supplier_name";
            case "unitName" -> "u.unit_name";
            default -> null;
        };

        if (col == null) {
            return " ORDER BY i.ingredient_name ASC, i.id ASC ";
        }

        String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return " ORDER BY " + col + " " + dir + ", i.id ASC ";
    }

    private IngredientSearchNativeResultDTO mapTupleToDto(Tuple tuple) {
        return IngredientSearchNativeResultDTO.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .ingredientCode(NativeSqlTupleUtils.getString(tuple, "ingredientCode"))
                .ingredientName(NativeSqlTupleUtils.getString(tuple, "ingredientName"))
                .selfLife(NativeSqlTupleUtils.getInteger(tuple, "selfLife"))
                .averagePrice(NativeSqlTupleUtils.getBigDecimal(tuple, "averagePrice"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .ingredientCategoryId(NativeSqlTupleUtils.getInteger(tuple, "ingredientCategoryId"))
                .ingredientCategoryName(NativeSqlTupleUtils.getString(tuple, "ingredientCategoryName"))
                .supplierId(NativeSqlTupleUtils.getInteger(tuple, "supplierId"))
                .supplierName(NativeSqlTupleUtils.getString(tuple, "supplierName"))
                .unitId(NativeSqlTupleUtils.getInteger(tuple, "unitId"))
                .unitName(NativeSqlTupleUtils.getString(tuple, "unitName"))
                .currentStock(NativeSqlTupleUtils.getInteger(tuple, "currentStock"))
                .build();
    }
}