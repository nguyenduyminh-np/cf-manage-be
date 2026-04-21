package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingAvailableSlotsRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCancelRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckInRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCheckOutRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingConfirmRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingDepositRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingExtendRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingWalkInRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingAvailableSlotResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface TableBookingService {

    // Tìm kiếm job
    public List<TableBookingResponseDTO> getPendingAndConfirmedBookings(TableBookingSearchRequestDTO request);

    /**
     * Tìm kiếm lịch sử đặt bàn theo bộ lọc và phân trang.
     */
    PageResponse<List<TableBookingResponseDTO>> search(TableBookingSearchRequestDTO request);

    /**
     * Tạo mới một booking cho bàn và đồng bộ trạng thái bàn.
     */
    TableBookingResponseDTO create(TableBookingCreateRequestDTO request);

    /**
     * Lấy chi tiết đầy đủ của một booking theo id.
     */
    TableBookingResponseDTO detail(TableBookingDetailRequestDTO request);

    /**
     * Cập nhật booking hiện có, hỗ trợ đổi bàn và đồng bộ trạng thái liên quan.
     */
    TableBookingResponseDTO update(TableBookingUpdateRequestDTO request);

    /**
     * Cập nhật trạng thái booking và tính lại trạng thái bàn.
     */
    Boolean updateStatus(TableBookingStatusUpdateRequestDTO request);

    TableBookingResponseDTO confirm(TableBookingConfirmRequestDTO request);

    TableBookingResponseDTO checkIn(TableBookingCheckInRequestDTO request);

    TableBookingResponseDTO checkOut(TableBookingCheckOutRequestDTO request);

    TableBookingResponseDTO cancel(TableBookingCancelRequestDTO request);

    TableBookingResponseDTO extend(TableBookingExtendRequestDTO request);

    TableBookingResponseDTO createWalkIn(TableBookingWalkInRequestDTO request, boolean force);

    TableBookingResponseDTO createWalkInFromLateArrival(
            Integer lateBookingId,
            TableBookingWalkInRequestDTO request,
            boolean force
    );

    TableBookingResponseDTO deposit(TableBookingDepositRequestDTO request);

    List<TableBookingAvailableSlotResponseDTO> getAvailableSlots(TableBookingAvailableSlotsRequestDTO request);

    void delete( Integer bookingId);
}
