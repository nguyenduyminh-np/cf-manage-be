package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckInRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckOutRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDepositRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingExtendRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;

import java.util.List;

public interface TableBookingService {

    /**
     * Tìm kiếm lịch sử đặt bàn theo bộ lọc và phân trang.
     */
    PageResponse<List<TableBookingResponseDTO>> search(TableBookingSearchRequestDTO request);

    /**
     * Tạo mới một booking cho bàn và đồng bộ trạng thái bàn.
     */
    TableBookingResponseDTO create(TableBookingCreateRequestDTO request);

    /**
     * Cập nhật booking hiện có, hỗ trợ đổi bàn và đồng bộ trạng thái liên quan.
     */
    TableBookingResponseDTO update(TableBookingUpdateRequestDTO request);

    /**
     * Cập nhật trạng thái booking và tính lại trạng thái bàn.
     */
    Boolean updateStatus(TableBookingStatusUpdateRequestDTO request);

    TableBookingResponseDTO confirm(Integer bookingId);

    TableBookingResponseDTO checkIn(Integer bookingId, TableBookingCheckInRequestDTO request);

    TableBookingResponseDTO checkOut(Integer bookingId, TableBookingCheckOutRequestDTO request);

    TableBookingResponseDTO cancel(Integer bookingId);

    TableBookingResponseDTO extend(Integer bookingId, TableBookingExtendRequestDTO request);

    TableBookingResponseDTO createWalkIn(TableBookingCreateRequestDTO request, boolean force);

    TableBookingResponseDTO deposit(Integer bookingId, TableBookingDepositRequestDTO request);
}
