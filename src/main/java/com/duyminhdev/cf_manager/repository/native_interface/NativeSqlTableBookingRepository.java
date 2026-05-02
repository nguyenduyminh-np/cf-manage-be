package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;

import java.util.List;
import java.util.Optional;

public interface NativeSqlTableBookingRepository {
    Optional<TableBookingDetailNativeResultDTO> findActiveBookingByTableId (Integer tableId);
    TableBookingDetailNativeResultDTO findBookingDetailByBookingId(Integer bookingId);
    List<TableBookingDetailNativeResultDTO> findPendingAndConfirmedBookings(Integer tableId);
}
