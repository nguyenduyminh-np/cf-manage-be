package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlInvoiceRepository;
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
import java.util.Optional;

@Repository
public class NativeSqlInvoiceRepositoryImpl implements NativeSqlInvoiceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<InvoiceSearchNativeResult> searchInvoices(InvoiceSearchRequestDTO request, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    i.id AS id,
                    i.invoice_code AS invoiceCode,
                    i.total_amount AS totalAmount,
                    i.payment_status AS paymentStatus,
                    i.payment_method AS paymentMethod,
                    i.created_at AS createdAt,
                    a.full_name AS fullName,
                    tb.booking_invoice_code AS bookingInvoiceCode
                FROM invoice i
                INNER JOIN account a ON a.id = i.account_id
                LEFT JOIN table_booking tb ON tb.id = i.booking_id
                WHERE i.is_active = 1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        // sorting
        String sortField = StringUtils.hasText(request.getSortField()) ? request.getSortField() : "createdAt";
        String sortDir = "desc".equalsIgnoreCase(request.getSortDir()) ? "DESC" : "ASC";
        switch (sortField) {
            case "invoiceCode":
                sql.append(" ORDER BY i.invoice_code ").append(sortDir);
                break;
            case "totalAmount":
                sql.append(" ORDER BY i.total_amount ").append(sortDir);
                break;
            case "paymentStatus":
                sql.append(" ORDER BY i.payment_status ").append(sortDir);
                break;
            case "paymentMethod":
                sql.append(" ORDER BY i.payment_method ").append(sortDir);
                break;
            case "createdAt":
            default:
                sql.append(" ORDER BY i.created_at ").append(sortDir);
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
    public long countInvoices(InvoiceSearchRequestDTO request) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM invoice i
                INNER JOIN account a ON a.id = i.account_id
                LEFT JOIN table_booking tb ON tb.id = i.booking_id
                WHERE i.is_active = 1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Map<String, Object> buildSearchParams(StringBuilder sql, InvoiceSearchRequestDTO request) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getInvoiceCode())) {
            sql.append(" AND i.invoice_code LIKE :invoiceCode");
            params.put("invoiceCode", "%" + request.getInvoiceCode().trim() + "%");
        }
        if (StringUtils.hasText(request.getPaymentStatus())) {
            sql.append(" AND i.payment_status = :paymentStatus");
            params.put("paymentStatus", request.getPaymentStatus().trim());
        }
        if (StringUtils.hasText(request.getPaymentMethod())) {
            sql.append(" AND i.payment_method = :paymentMethod");
            params.put("paymentMethod", request.getPaymentMethod().trim());
        }
        if (request.getTotalAmountFrom() != null) {
            sql.append(" AND i.total_amount >= :totalAmountFrom");
            params.put("totalAmountFrom", request.getTotalAmountFrom());
        }
        if (request.getTotalAmountTo() != null) {
            sql.append(" AND i.total_amount <= :totalAmountTo");
            params.put("totalAmountTo", request.getTotalAmountTo());
        }
        return params;
    }


    private InvoiceSearchNativeResult mapSearchRow(Tuple tuple) {
        return InvoiceSearchNativeResult.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .invoiceCode(NativeSqlTupleUtils.getString(tuple, "invoiceCode"))
                .totalAmount(NativeSqlTupleUtils.getBigDecimal(tuple, "totalAmount"))
                .paymentStatus(NativeSqlTupleUtils.getString(tuple, "paymentStatus"))
                .paymentMethod(NativeSqlTupleUtils.getString(tuple, "paymentMethod"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .fullName(NativeSqlTupleUtils.getString(tuple, "fullName"))
                .bookingInvoiceCode(NativeSqlTupleUtils.getString(tuple, "bookingInvoiceCode"))
                .build();
    }

    @Override
    public Optional<InvoiceDetailNativeResult> findInvoiceDetailById(Integer invoiceId) {
        String sql = """
                SELECT
                    i.id AS invoiceId,
                    i.invoice_code AS invoiceCode,
                    i.total_amount AS totalAmount,
                    i.payment_status AS paymentStatus,
                    i.payment_method AS paymentMethod,
                    i.created_at AS createdAt,
                    dt.id AS tableId,
                    dt.table_code AS tableCode,
                    dt.table_name AS tableName,
                    dt.floor AS floor,
                    dt.slot AS slot,
                    dt.table_status AS tableStatus,
                    a.id AS accountId,
                    a.username AS username,
                    a.full_name AS fullName,
                    tb.id AS bookingId,
                    tb.customer_name AS customerName,
                    tb.phone_number AS phoneNumber
                FROM invoice i
                INNER JOIN dining_table dt ON dt.id = i.dining_table_id
                INNER JOIN account a ON a.id = i.account_id
                LEFT JOIN table_booking tb ON tb.id = i.booking_id
                WHERE i.id = :invoiceId AND i.is_active = 1
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("invoiceId", invoiceId);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        if (tuples.isEmpty()) return Optional.empty();
        Tuple tuple = tuples.get(0);
        return Optional.of(mapDetailRow(tuple));
    }


    @Override
    public List<InvoiceDetailNativeResultDTO> findInvoiceDetailByInvoiceId(Integer invoiceId) {
        String sql = """
                SELECT
                    i.id AS invoiceId,
                    i.invoice_code AS invoiceCode,
                    i.dish_order_id AS dishOrderId,
                    dt.id AS tableId,
                    dt.table_code AS tableCode,
                    dt.table_name AS tableName,
                    a.id AS accountId,
                    a.username AS accountUsername,
                    a.full_name AS accountFullName,
                    i.payment_status AS paymentStatus,
                    i.payment_method AS paymentMethod,
                    i.guest_count AS guestCount,
                    i.total_amount AS totalMoney,
                    i.created_at AS createdTime,
                    i.booking_id AS bookingId,
                    i.customer_name AS customerName,
                    i.customer_phone AS customerPhone,
                    idt.id AS invoiceDetailId,
                    d.id AS dishId,
                    d.dish_code AS dishCode,
                    d.dish_name AS dishName,
                    idt.quantity AS quantity,
                    idt.unit_price AS unitPrice,
                    (idt.quantity * idt.unit_price) AS lineTotal
                FROM invoice i
                INNER JOIN dining_table dt
                    ON dt.id = i.dining_table_id
                INNER JOIN account a
                    ON a.id = i.account_id
                LEFT JOIN invoice_detail idt
                    ON idt.invoice_id = i.id
                LEFT JOIN dish d
                    ON d.id = idt.dish_id
                WHERE i.id = :invoiceId
                ORDER BY idt.id ASC
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("invoiceId", invoiceId);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        return tuples.stream()
                .map(this::mapTupleToDto)
                .toList();
    }

    private InvoiceDetailNativeResult mapDetailRow(Tuple tuple) {
        return InvoiceDetailNativeResult.builder()
                .invoiceId(NativeSqlTupleUtils.getInteger(tuple, "invoiceId"))
                .invoiceCode(NativeSqlTupleUtils.getString(tuple, "invoiceCode"))
                .totalAmount(NativeSqlTupleUtils.getBigDecimal(tuple, "totalAmount"))
                .paymentStatus(NativeSqlTupleUtils.getString(tuple, "paymentStatus"))
                .paymentMethod(NativeSqlTupleUtils.getString(tuple, "paymentMethod"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .tableId(NativeSqlTupleUtils.getInteger(tuple, "tableId"))
                .tableCode(NativeSqlTupleUtils.getString(tuple, "tableCode"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .floor(NativeSqlTupleUtils.getInteger(tuple, "floor"))
                .slot(NativeSqlTupleUtils.getInteger(tuple, "slot"))
                .tableStatus(NativeSqlTupleUtils.getString(tuple, "tableStatus"))
                .accountId(NativeSqlTupleUtils.getInteger(tuple, "accountId"))
                .username(NativeSqlTupleUtils.getString(tuple, "username"))
                .fullName(NativeSqlTupleUtils.getString(tuple, "fullName"))
                .bookingId(NativeSqlTupleUtils.getInteger(tuple, "bookingId"))
                .customerName(NativeSqlTupleUtils.getString(tuple, "customerName"))
                .phoneNumber(NativeSqlTupleUtils.getString(tuple, "phoneNumber"))
                .build();
    }

    private InvoiceDetailNativeResultDTO mapTupleToDto(Tuple tuple) {
        return InvoiceDetailNativeResultDTO.builder()
                .invoiceId(NativeSqlTupleUtils.getInteger(tuple, "invoiceId"))
                .invoiceCode(NativeSqlTupleUtils.getString(tuple, "invoiceCode"))
                .dishOrderId(NativeSqlTupleUtils.getLong(tuple, "dishOrderId"))
                .tableId(NativeSqlTupleUtils.getInteger(tuple, "tableId"))
                .tableCode(NativeSqlTupleUtils.getString(tuple, "tableCode"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .accountId(NativeSqlTupleUtils.getInteger(tuple, "accountId"))
                .accountUsername(NativeSqlTupleUtils.getString(tuple, "accountUsername"))
                .accountFullName(NativeSqlTupleUtils.getString(tuple, "accountFullName"))
                .paymentStatus(NativeSqlTupleUtils.getString(tuple, "paymentStatus"))
                .paymentMethod(NativeSqlTupleUtils.getString(tuple, "paymentMethod"))
                .guestCount(NativeSqlTupleUtils.getInteger(tuple, "guestCount"))
                .totalMoney(NativeSqlTupleUtils.getBigDecimal(tuple, "totalMoney"))
                .createdTime(NativeSqlTupleUtils.getInstant(tuple, "createdTime"))
                .bookingId(NativeSqlTupleUtils.getInteger(tuple, "bookingId"))
                .customerName(NativeSqlTupleUtils.getString(tuple, "customerName"))
                .customerPhone(NativeSqlTupleUtils.getString(tuple, "customerPhone"))
                .invoiceDetailId(NativeSqlTupleUtils.getInteger(tuple, "invoiceDetailId"))
                .dishId(NativeSqlTupleUtils.getInteger(tuple, "dishId"))
                .dishCode(NativeSqlTupleUtils.getString(tuple, "dishCode"))
                .dishName(NativeSqlTupleUtils.getString(tuple, "dishName"))
                .quantity(NativeSqlTupleUtils.getInteger(tuple, "quantity"))
                .unitPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "unitPrice"))
                .lineTotal(NativeSqlTupleUtils.getBigDecimal(tuple, "lineTotal"))
                .build();
    }
}
