package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.service.TableBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/table-booking")
@RequiredArgsConstructor
public class TableBookingController {

    private final TableBookingService tableBookingService;

    /**
     * Tim kiem lich su dat ban theo bo loc va phan trang.
     */
    @PostMapping("/search")
    public ApiResponse<PageResponse<List<TableBookingResponseDTO>>> search(
            @Valid @RequestBody TableBookingSearchRequestDTO request
    ) {
        /**
         * Flow API booking search:
         * 1. Nhan dieu kien tim kiem booking
         * 2. Goi service query theo specification
         * 3. Tra ket qua page cho FE
         */
        return new ApiResponse<>(200, "SEARCH_TABLE_BOOKING_SUCCESS", tableBookingService.search(request));
    }

    /**
     * Tao moi booking cho ban.
     */
    @PostMapping("/create")
    public ApiResponse<TableBookingResponseDTO> create(
            @Valid @RequestBody TableBookingCreateRequestDTO request
    ) {
        /**
         * Flow API booking create:
         * 1. Nhan thong tin dat ban moi
         * 2. Goi service tao booking va dong bo trang thai ban
         * 3. Tra booking vua tao
         */
        return new ApiResponse<>(200, "CREATE_TABLE_BOOKING_SUCCESS", tableBookingService.create(request));
    }

    /**
     * Cap nhat thong tin booking hien co.
     */
    @PostMapping("/update")
    public ApiResponse<TableBookingResponseDTO> update(
            @Valid @RequestBody TableBookingUpdateRequestDTO request
    ) {
        /**
         * Flow API booking update:
         * 1. Nhan bookingId va thong tin can cap nhat
         * 2. Goi service cap nhat booking
         * 3. Tra booking sau cap nhat
         */
        return new ApiResponse<>(200, "UPDATE_TABLE_BOOKING_SUCCESS", tableBookingService.update(request));
    }

    /**
     * Cap nhat trang thai booking.
     */
    @PostMapping("/update-status")
    public ApiResponse<Boolean> updateStatus(
            @Valid @RequestBody TableBookingStatusUpdateRequestDTO request
    ) {
        /**
         * Flow API booking update status:
         * 1. Nhan bookingId va status moi
         * 2. Goi service cap nhat trang thai booking
         * 3. Tra ket qua true/false cho FE
         */
        return new ApiResponse<>(200, "UPDATE_TABLE_BOOKING_STATUS_SUCCESS", tableBookingService.updateStatus(request));
    }
}
