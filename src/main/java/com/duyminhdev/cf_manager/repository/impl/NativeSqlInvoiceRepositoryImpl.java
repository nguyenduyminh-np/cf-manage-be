package com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.repository.NativeSqlInvoiceRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NativeSqlInvoiceRepositoryImpl implements NativeSqlInvoiceRepository {

    @PersistenceContext
    private EntityManager entityManager;

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
