package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishCategorySearchNativeResult;

import com.duyminhdev.cf_manager.dto.request.dish_category.DishCategorySearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlDishCategoryRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NativeSqlDishCategoryRepositoryImpl implements NativeSqlDishCategoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DishCategorySearchNativeResult> searchCategories(DishCategorySearchRequestDTO request, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    d.id AS id,
                    d.dish_category_code AS dishCategoryCode,
                    d.dish_category_name AS dishCategoryName,
                    d.created_at AS createdTime,
                    d.is_active AS active
                FROM dish_category d
                WHERE 1=1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        // Sắp xếp
        String sortField = StringUtils.hasText(request.getSortField()) ? request.getSortField() : "createdTime";
        String sortDir = "desc".equalsIgnoreCase(request.getSortDir()) ? "DESC" : "ASC";
        switch (sortField) {
            case "dishCategoryCode":
                sql.append(" ORDER BY d.dish_category_code ").append(sortDir);
                break;
            case "dishCategoryName":
                sql.append(" ORDER BY d.dish_category_name ").append(sortDir);
                break;
            case "active":
                sql.append(" ORDER BY d.is_active ").append(sortDir);
                break;
            case "createdTime":
            default:
                sql.append(" ORDER BY d.created_at ").append(sortDir);
                break;
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
    public long countCategories(DishCategorySearchRequestDTO request) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM dish_category d
                WHERE 1=1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Map<String, Object> buildSearchParams(StringBuilder sql, DishCategorySearchRequestDTO request) {
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.hasText(request.getDishCategoryCode())) {
            sql.append(" AND d.dish_category_code LIKE :dishCategoryCode");
            params.put("dishCategoryCode", "%" + request.getDishCategoryCode().trim() + "%");
        }
        if (StringUtils.hasText(request.getDishCategoryName())) {
            sql.append(" AND d.dish_category_name LIKE :dishCategoryName");
            params.put("dishCategoryName", "%" + request.getDishCategoryName().trim() + "%");
        }
        if (request.getFromDate() != null) {
            sql.append(" AND d.created_at >= :fromDate");
            params.put("fromDate", request.getFromDate());
        }
        if (request.getToDate() != null) {
            sql.append(" AND d.created_at <= :toDate");
            params.put("toDate", request.getToDate());
        }
        if (request.getIsActive() != null) {
            sql.append(" AND d.is_active = :isActive");
            params.put("isActive", request.getIsActive());
        }
        return params;
    }

    private DishCategorySearchNativeResult mapSearchRow(Tuple tuple) {
        return DishCategorySearchNativeResult.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .dishCategoryCode(NativeSqlTupleUtils.getString(tuple, "dishCategoryCode"))
                .dishCategoryName(NativeSqlTupleUtils.getString(tuple, "dishCategoryName"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .build();
    }
}