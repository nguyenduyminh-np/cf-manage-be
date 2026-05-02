package com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;
import com.duyminhdev.cf_manager.repository.NativeSqlTableBookingRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class NativeSqlTableBookingRepositoryImpl implements NativeSqlTableBookingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TableBookingDetailNativeResultDTO> findActiveBookingByTableId(Integer tableId) {
        String sql = """
        SELECT
            tb.id AS bookingId,
            tb.customer_name AS customerName,
            tb.phone_number AS phoneNumber,
            dt.slot AS tableSlot
        FROM table_booking tb
        INNER JOIN dining_table dt ON dt.id = tb.dining_table_id
        WHERE tb.dining_table_id = :tableId
          AND tb.is_active = 1
          AND tb.booking_status IN ('CONFIRMED', 'CHECKED_IN')
          AND tb.expected_arrive_time <= NOW()
          AND tb.check_out_at IS NULL
        ORDER BY tb.expected_arrive_time DESC
        LIMIT 1
        """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("tableId", tableId);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        if(tuples.isEmpty()){
            return Optional.empty();
        }
        Tuple tuple = tuples.get(0);
        return Optional.of(TableBookingDetailNativeResultDTO.builder()
                .bookingId(NativeSqlTupleUtils.getInteger(tuple, "bookingId"))
                .customerName(NativeSqlTupleUtils.getString(tuple, "customerName"))
                .phoneNumber(NativeSqlTupleUtils.getString(tuple, "phoneNumber"))
                .build());
    }

    @Override
    public TableBookingDetailNativeResultDTO findBookingDetailByBookingId(Integer bookingId) {
        String sql = """
                SELECT
                    tb.id AS bookingId,
                    dt.id AS tableId,
                    dt.table_code AS tableCode,
                    dt.table_name AS tableName,
                    tb.expected_arrive_time AS expectedArriveTime,
                    tb.check_in_at AS checkInAt,
                    tb.expected_check_out AS expectedCheckOut,
                    tb.check_out_at AS checkOutAt,
                    tb.booking_status AS bookingStatus,
                    tb.customer_name AS customerName,
                    tb.phone_number AS phoneNumber,
                    tb.deposit_amount AS depositAmount,
                    tb.deposit_paid AS depositPaid,
                    tb.deposit_paid_at AS depositPaidAt,
                    tb.is_deposit_forfeited AS depositForfeited,
                    tb.deposit_txn_ref AS depositTxnRef,
                    tb.note AS note,
                    tb.booking_invoice_code AS bookingInvoiceCode,
                    a.id AS accountId,
                    a.username AS accountUsername,
                    a.full_name AS accountFullName,
                    tb.is_active AS active,
                    tb.created_at AS createdAt
                FROM table_booking tb
                INNER JOIN dining_table dt
                    ON dt.id = tb.dining_table_id
                INNER JOIN account a
                    ON a.id = tb.account_id
                WHERE tb.id = :bookingId
                  AND (tb.is_active = 1 OR tb.is_active = true)
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("bookingId", bookingId);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        if (tuples == null || tuples.isEmpty()) {
            return null;
        }

        return mapTupleToDto(tuples.get(0));
    }

    @Override
    public List<TableBookingDetailNativeResultDTO> findPendingAndConfirmedBookings(Integer tableId) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT
                    tb.id AS bookingId,
                    dt.id AS tableId,
                    dt.table_code AS tableCode,
                    dt.table_name AS tableName,
                    tb.expected_arrive_time AS expectedArriveTime,
                    tb.check_in_at AS checkInAt,
                    tb.expected_check_out AS expectedCheckOut,
                    tb.check_out_at AS checkOutAt,
                    tb.booking_status AS bookingStatus,
                    tb.customer_name AS customerName,
                    tb.phone_number AS phoneNumber,
                    tb.deposit_amount AS depositAmount,
                    tb.deposit_paid AS depositPaid,
                    tb.deposit_paid_at AS depositPaidAt,
                    tb.is_deposit_forfeited AS depositForfeited,
                    tb.deposit_txn_ref AS depositTxnRef,
                    tb.note AS note,
                    tb.booking_invoice_code AS bookingInvoiceCode,
                    a.id AS accountId,
                    a.username AS accountUsername,
                    a.full_name AS accountFullName,
                    tb.is_active AS active,
                    tb.created_at AS createdAt
                FROM table_booking tb
                INNER JOIN dining_table dt
                    ON dt.id = tb.dining_table_id
                INNER JOIN account a
                    ON a.id = tb.account_id
                WHERE tb.booking_status IN ('PENDING_CONFIRMATION', 'CONFIRMED')
                  AND tb.is_active = true
                """);

        if (tableId != null) {
            sqlBuilder.append(" AND dt.id = :tableId");
        }

        sqlBuilder.append(" ORDER BY tb.expected_arrive_time ASC");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Tuple.class);

        if (tableId != null) {
            query.setParameter("tableId", tableId);
        }

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        List<TableBookingDetailNativeResultDTO> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            result.add(mapTupleToDto(tuple));
        }
        return result;
    }

    private TableBookingDetailNativeResultDTO mapTupleToDto(Tuple tuple) {
        return TableBookingDetailNativeResultDTO.builder()
                .bookingId(NativeSqlTupleUtils.getInteger(tuple, "bookingId"))
                .bookingInvoiceCode(NativeSqlTupleUtils.getString(tuple, "bookingInvoiceCode"))
                .tableId(NativeSqlTupleUtils.getInteger(tuple, "tableId"))
                .tableCode(NativeSqlTupleUtils.getString(tuple, "tableCode"))
                .tableName(NativeSqlTupleUtils.getString(tuple, "tableName"))
                .expectedArriveTime(NativeSqlTupleUtils.getInstant(tuple, "expectedArriveTime"))
                .checkInAt(NativeSqlTupleUtils.getInstant(tuple, "checkInAt"))
                .expectedCheckOut(NativeSqlTupleUtils.getInstant(tuple, "expectedCheckOut"))
                .checkOutAt(NativeSqlTupleUtils.getInstant(tuple, "checkOutAt"))
                .bookingStatus(NativeSqlTupleUtils.getString(tuple, "bookingStatus"))
                .customerName(NativeSqlTupleUtils.getString(tuple, "customerName"))
                .phoneNumber(NativeSqlTupleUtils.getString(tuple, "phoneNumber"))
                .depositAmount(NativeSqlTupleUtils.getBigDecimal(tuple, "depositAmount"))
                .depositPaid(NativeSqlTupleUtils.getBoolean(tuple, "depositPaid"))
                .depositPaidAt(NativeSqlTupleUtils.getInstant(tuple, "depositPaidAt"))
                .depositForfeited(NativeSqlTupleUtils.getBoolean(tuple, "depositForfeited"))
                .depositTxnRef(NativeSqlTupleUtils.getString(tuple, "depositTxnRef"))
                .note(NativeSqlTupleUtils.getString(tuple, "note"))
                .accountId(NativeSqlTupleUtils.getInteger(tuple, "accountId"))
                .accountUsername(NativeSqlTupleUtils.getString(tuple, "accountUsername"))
                .accountFullName(NativeSqlTupleUtils.getString(tuple, "accountFullName"))
                .active(NativeSqlTupleUtils.getBoolean(tuple, "active"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .build();
    }
}
