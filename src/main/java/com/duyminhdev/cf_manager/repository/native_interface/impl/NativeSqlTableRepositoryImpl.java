package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlTableRepository;
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
                MAX(tb.expected_arrive_time) AS lastBookingTime,
                dt.is_active AS active
            """;

    private static final String FROM_WHERE = """
            FROM cafe_table dt
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

    private static final String SELECT_AVAILABLE_TABLES = """
            SELECT
                                dt.id AS tableId,
                dt.table_name AS tableName,
                dt.table_code AS tableCode,
                                dt.table_status AS tableStatus,
                                dt.floor AS floor,
                                dt.slot AS slot
            FROM cafe_table dt
            WHERE (dt.is_active = 1 OR dt.is_active = true)
              AND UPPER(dt.table_status) = :tableStatus
            """;

    @Override
    public PageResponse<List<TableSearchNativeResultDTO>> search(TableSearchRequestDTO request) {
        TableSearchRequestDTO safeRequest = request != null ? request : new TableSearchRequestDTO();

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

        List<TableSearchNativeResultDTO> data = tuples.stream()
                .map(this::mapTupleToDto)
                .toList();

        long totalElements = countTotalElements(safeRequest);
        int totalPages = PageUtils.calculateTotalPages(totalElements, pageSize);

        PageResponse<List<TableSearchNativeResultDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    @Override
    public List<TableSearchNativeResultDTO> searchAll(TableSearchRequestDTO request) {
        TableSearchRequestDTO safeRequest = request != null ? request : new TableSearchRequestDTO();

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(safeRequest, params);
        String orderBy = buildOrderBy(safeRequest.getSortField(), safeRequest.getSortDir());

        String dataSql = SELECT_COLUMNS
                + FROM_WHERE
                + whereClause
                + GROUP_BY
                + orderBy;

        Query dataQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        bindParameters(dataQuery, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = dataQuery.getResultList();

        return tuples.stream()
                .map(this::mapTupleToDto)
                .toList();
    }

    @Override
    public List<TableAvailableNativeResultDTO> findAvailableTables(TableAvailableSearchRequestDTO request) {
        TableAvailableSearchRequestDTO safeRequest = request != null ? request : new TableAvailableSearchRequestDTO();

        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildAvailableWhereClause(safeRequest, params);
        String sql = SELECT_AVAILABLE_TABLES + whereClause + " ORDER BY dt.floor ASC, dt.slot ASC, dt.id ASC ";

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("tableStatus", TableStatusEnum.AVAILABLE.getCode());
        bindParameters(query, params);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        return tuples.stream()
                .map(this::mapTupleToAvailableDto)
                .toList();
    }

    private String buildAvailableWhereClause(TableAvailableSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) {
            return sql.toString();
        }

        if (StringUtils.hasText(request.getTableName())) {
            sql.append("""
                     AND (
                        LOWER(dt.table_name) LIKE :tableNameKeyword ESCAPE '\\\\'
                        OR TRIM(
                            CASE
                                WHEN LOWER(dt.table_name) LIKE 'bàn %' THEN SUBSTRING(LOWER(dt.table_name), 5)
                                ELSE LOWER(dt.table_name)
                            END
                        ) LIKE :tableNameKeyword ESCAPE '\\\\'
                     )
                    """);
            String tableNameKeyword = "%" + NativeSqlTupleUtils.escapeLike(request.getTableName().trim().toLowerCase()) + "%";
            params.put("tableNameKeyword", tableNameKeyword);
        }

        if (request.getFloor() != null) {
            sql.append(" AND dt.floor = :floor ");
            params.put("floor", request.getFloor());
        }

        if (request.getSeat() != null) {
            sql.append(" AND dt.slot = :seat ");
            params.put("seat", request.getSeat());
        }

        return sql.toString();
    }

    private long countTotalElements(TableSearchRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();

        String countSql = """
                SELECT COUNT(1)
                FROM cafe_table dt
                WHERE 1 = 1
                """ + buildWhereClause(request, params);

        Query countQuery = entityManager.createNativeQuery(countSql);
        bindParameters(countQuery, params);

        Object result = countQuery.getSingleResult();
        return ((Number) result).longValue();
    }

    private String buildWhereClause(TableSearchRequestDTO request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) {
            return sql.toString();
        }

        if (StringUtils.hasText(request.getKeyword())) {
            sql.append("""
                     AND (
                        LOWER(dt.table_name) LIKE :tableNameKeyword ESCAPE '\\\\'
                        OR TRIM(
                            CASE
                                WHEN LOWER(dt.table_name) LIKE 'bàn %' THEN SUBSTRING(LOWER(dt.table_name), 5)
                                ELSE LOWER(dt.table_name)
                            END
                        ) LIKE :tableNameKeyword ESCAPE '\\\\'
                     )
                    """);
            String tableNameKeyword = "%" + NativeSqlTupleUtils.escapeLike(request.getKeyword().trim().toLowerCase()) + "%";
            params.put("tableNameKeyword", tableNameKeyword);
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

    private void bindParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String dbColumn = mapSortFieldToDbColumn(sortField);

        if (!StringUtils.hasText(dbColumn)) {
            return " ORDER BY dt.table_name ASC, dt.id ASC ";
        }

        String direction = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return " ORDER BY " + dbColumn + " " + direction + ", dt.id DESC ";
    }

    private String mapSortFieldToDbColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return null;
        }

        return switch (sortField.trim()) {
            case "id" -> "dt.id";
            case "tableCode" -> "dt.table_code";
            case "tableName" -> "dt.table_name";
            case "tableStatus" -> "dt.table_status";
            case "floor" -> "dt.floor";
            case "slot" -> "dt.slot";
            case "totalBooking" -> "totalBooking";
            case "lastBookingTime" -> "lastBookingTime";
            case "active" -> "dt.is_active";
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
                .lastBookingTime(NativeSqlTupleUtils.getInstant(tuple, "lastBookingTime"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .build();
    }

    private TableAvailableNativeResultDTO mapTupleToAvailableDto(Tuple tuple) {
        return TableAvailableNativeResultDTO.builder()
                .tableId(NativeSqlTupleUtils.getInteger(tuple, "tableId"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .tableCode(NativeSqlTupleUtils.getString(tuple, "tableCode"))
                .tableStatus(NativeSqlTupleUtils.getString(tuple, "tableStatus"))
                .floor(NativeSqlTupleUtils.getInteger(tuple, "floor"))
                .slot(NativeSqlTupleUtils.getInteger(tuple, "slot"))
                .build();
    }
}