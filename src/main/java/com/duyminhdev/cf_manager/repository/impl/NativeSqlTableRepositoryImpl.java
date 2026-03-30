package main.java.com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.utils.PageUtils;
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
public class NativeSqlTableRepositoryImpl implements NativeSqlTableRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_COLUMNS = """
            SELECT
                dt.id AS tableId,
                dt.table_code AS tableCode,
                dt.table_name AS tableName,
                dt.table_status AS tableStatus,
                dt.floor AS floor,
                dt.slot AS slot,
                COALESCE(COUNT(tb.id), 0) AS totalBooking,
                MAX(tb.booking_at) AS lastBookingTime,
                dt.is_active AS active
            """;

    private static final String FROM_WHERE = """
            FROM dining_table dt
            LEFT JOIN table_booking tb
                ON tb.dining_table_id = dt.id
               AND (tb.is_active = 1 OR tb.is_active = true)
            WHERE 1 = 1
            """;

    private static final String GROUP_BY = """
            GROUP BY
                dt.id,
                dt.table_code,
                dt.table_name,
                dt.table_status,
                dt.floor,
                dt.slot,
                dt.is_active
            """;

    @Override
    public PageResponse<List<TableSearchNativeResultDTO>> search(TableSearchRequestDTO request) {
        int pageNo = PageUtils.normalizePage(request.getPage());
        int pageSize = PageUtils.normalizeLimit(request.getLimit());

        Map<String, Object> params = new HashMap<>();
        String whereClause = buildWhere(request, params);
        String orderBy = buildOrderBy(request.getSortField(), request.getSortDir());

        String dataSql = SELECT_COLUMNS + FROM_WHERE + whereClause + GROUP_BY + orderBy;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        params.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult(pageNo * pageSize);
        dataQuery.setMaxResults(pageSize);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();

        List<TableSearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .toList();

        long totalElements = countTotalElements(request);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<TableSearchNativeResultDTO>> response = new PageResponse<>();
        response.setData(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    private long countTotalElements(TableSearchRequestDTO request) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1)
                FROM dining_table dt
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();
        sql.append(buildCountWhere(request, params));

        Query countQuery = entityManager.createNativeQuery(sql.toString());
        params.forEach(countQuery::setParameter);

        Object result = countQuery.getSingleResult();
        return ((Number) result).longValue();
    }

    private String buildWhere(TableSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (StringUtils.hasText(request.getKeyword())) {
            sql.append("""
                     AND (
                        LOWER(dt.table_code) LIKE :keyword ESCAPE '\\'
                        OR LOWER(dt.table_name) LIKE :keyword ESCAPE '\\'
                     )
                    """);
            params.put("keyword", NativeSqlTupleUtils.escapeLike(request.getKeyword().trim().toLowerCase()) + "%");
        }

        if (request.getFloor() != null) {
            sql.append(" AND dt.floor = :floor ");
            params.put("floor", request.getFloor());
        }

        if (request.getSlot() != null) {
            sql.append(" AND dt.slot = :slot ");
            params.put("slot", request.getSlot());
        }

        if (StringUtils.hasText(request.getTableStatus())) {
            sql.append(" AND UPPER(dt.table_status) = :tableStatus ");
            params.put("tableStatus", request.getTableStatus().trim().toUpperCase());
        }

        if (request.getActive() != null) {
            sql.append(" AND dt.is_active = :active ");
            params.put("active", request.getActive());
        }

        return sql.toString();
    }

    private String buildCountWhere(TableSearchRequestDTO request, Map<String, Object> params) {
        return buildWhere(request, params);
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String dbColumn = mapSortFieldToDbColumn(sortField);
        String direction = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return " ORDER BY " + (dbColumn != null ? dbColumn : "dt.id") + " " + direction + " ";
    }

    private String mapSortFieldToDbColumn(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return null;
        }

        return switch (sortField) {
            case "tableCode" -> "dt.table_code";
            case "tableName" -> "dt.table_name";
            case "tableStatus" -> "dt.table_status";
            case "floor" -> "dt.floor";
            case "slot" -> "dt.slot";
            case "totalBooking" -> "totalBooking";
            case "lastBookingTime" -> "lastBookingTime";
            case "id" -> "dt.id";
            default -> null;
        };
    }

    private TableSearchNativeResultDTO mapTupleToDto(Tuple tuple) {
        return TableSearchNativeResultDTO.builder()
                .tableId(NativeSqlTupleUtils.getInteger(tuple, "tableId"))
                .tableCode(NativeSqlTupleUtils.getString(tuple, "tableCode"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .tableStatus(NativeSqlTupleUtils.getString(tuple, "tableStatus"))
                .floor(NativeSqlTupleUtils.getInteger(tuple, "floor"))
                .slot(NativeSqlTupleUtils.getInteger(tuple, "slot"))
                .totalBooking(NativeSqlTupleUtils.getInteger(tuple, "totalBooking"))
                .lastBookingTime(NativeSqlTupleUtils.getLocalDateTime(tuple, "lastBookingTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .build();
    }
}
