package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientCategorySearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.ingredient_category.IngredientCategorySearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlIngredientCategoryRepository;
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
public class NativeSqlIngredientCategoryRepositoryImpl implements NativeSqlIngredientCategoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
        SELECT
            ic.id AS id,
            ic.ingredient_category_code AS ingredientCategoryCode,
            ic.ingredient_category_name AS ingredientCategoryName,
            ic.created_at AS createdTime,
            ic.is_active AS active,
            ic.parent_category_id AS parentCategoryId,
            pc.ingredient_category_name AS parentCategoryName,
            (SELECT COUNT(*) FROM ingredient i
             WHERE i.ingredient_category_id = ic.id AND i.is_active = 1) AS ingredientCount
    """;

    private static final String FROM_JOIN = """
        FROM ingredient_category ic
        LEFT JOIN ingredient_category pc ON ic.parent_category_id = pc.id
    """;

    @Override
    public PageResponse<List<IngredientCategorySearchNativeResultDTO>> search(IngredientCategorySearchRequestDTO request) {
        request = request != null ? request : new IngredientCategorySearchRequestDTO();
        int pageNo = PageUtils.normalizePage(request.getPage());
        int pageSize = PageUtils.normalizeLimit(request.getLimit());
        int offset = pageNo * pageSize;

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String orderBy = buildOrderBy(request.getSortField(), request.getSortDir());

        String dataSql = SELECT_COLUMNS + FROM_JOIN + whereClause + orderBy
                + " LIMIT " + offset + ", " + pageSize;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();
        List<IngredientCategorySearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .collect(Collectors.toList());

        long totalElements = countTotalElements(request);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<IngredientCategorySearchNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    @Override
    public List<IngredientCategorySearchNativeResultDTO> findAllByCriteria(IngredientCategorySearchRequestDTO request) {
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

    private long countTotalElements(IngredientCategorySearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(request, params);
        String countSql = "SELECT COUNT(1) FROM ingredient_category ic " +
                          "LEFT JOIN ingredient_category pc ON ic.parent_category_id = pc.id " +
                          whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private String buildWhereClause(IngredientCategorySearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("WHERE 1=1 ");

        if (request.getActive() != null) {
            sql.append(" AND ic.is_active = :active ");
            params.put("active", request.getActive() ? 1 : 0);
        }

        if (request.getParentCategoryId() != null) {
            sql.append(" AND ic.parent_category_id = :parentCategoryId ");
            params.put("parentCategoryId", request.getParentCategoryId());
        }

        if (StringUtils.hasText(request.getSearchString())) {
            sql.append(" AND (LOWER(ic.ingredient_category_code) LIKE :keyword ESCAPE '\\\\' "
                     + "OR LOWER(ic.ingredient_category_name) LIKE :keyword ESCAPE '\\\\') ");
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
            case "id" -> "ic.id";
            case "ingredientCategoryCode" -> "ic.ingredient_category_code";
            case "ingredientCategoryName" -> "ic.ingredient_category_name";
            case "createdTime" -> "ic.created_at";
            case "active" -> "ic.is_active";
            case "parentCategoryName" -> "pc.ingredient_category_name";
            case "ingredientCount" -> "ingredientCount";
            default -> null;
        };

        if (col == null) {
            return "ORDER BY ic.ingredient_category_name ASC, ic.id ASC";
        }

        String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return "ORDER BY " + col + " " + dir + ", ic.id ASC";
    }

    private IngredientCategorySearchNativeResultDTO mapTupleToDto(Tuple tuple) {
        return IngredientCategorySearchNativeResultDTO.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .ingredientCategoryCode(NativeSqlTupleUtils.getString(tuple, "ingredientCategoryCode"))
                .ingredientCategoryName(NativeSqlTupleUtils.getString(tuple, "ingredientCategoryName"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .parentCategoryId(NativeSqlTupleUtils.getInteger(tuple, "parentCategoryId"))
                .parentCategoryName(NativeSqlTupleUtils.getString(tuple, "parentCategoryName"))
                .ingredientCount(NativeSqlTupleUtils.getInteger(tuple, "ingredientCount"))
                .build();
    }
}
