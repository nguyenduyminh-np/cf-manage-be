package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;

import java.util.List;

public interface NativeSqlTableBookingRepository {

    TableBookingDetailNativeResultDTO findBookingDetailByBookingId(Integer bookingId);
    List<TableBookingDetailNativeResultDTO> findPendingAndConfirmedBookings(Integer tableId);
}
