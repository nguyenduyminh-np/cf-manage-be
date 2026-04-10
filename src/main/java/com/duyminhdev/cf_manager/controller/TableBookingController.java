package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
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
import com.duyminhdev.cf_manager.service.TableBookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/table-booking")
@RequiredArgsConstructor
@Validated
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

    @PostMapping("/{id}/confirm")
    public ApiResponse<TableBookingResponseDTO> confirm(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId
    ) {
        return new ApiResponse<>(200, "CONFIRM_TABLE_BOOKING_SUCCESS", tableBookingService.confirm(bookingId));
    }

    @PostMapping("/{id}/check-in")
    public ApiResponse<TableBookingResponseDTO> checkIn(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @RequestBody(required = false) TableBookingCheckInRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_IN_TABLE_BOOKING_SUCCESS", tableBookingService.checkIn(bookingId, request));
    }

    @PostMapping("/{id}/check-out")
    public ApiResponse<TableBookingResponseDTO> checkOut(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @RequestBody(required = false) TableBookingCheckOutRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_OUT_TABLE_BOOKING_SUCCESS", tableBookingService.checkOut(bookingId, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TableBookingResponseDTO> cancel(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId
    ) {
        return new ApiResponse<>(200, "CANCEL_TABLE_BOOKING_SUCCESS", tableBookingService.cancel(bookingId));
    }

    @PostMapping("/{id}/deposit")
    public ApiResponse<TableBookingResponseDTO> deposit(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @Valid @RequestBody TableBookingDepositRequestDTO request
    ) {
        return new ApiResponse<>(200, "DEPOSIT_TABLE_BOOKING_SUCCESS", tableBookingService.deposit(bookingId, request));
    }

    @PostMapping("/{id}/extend")
    public ApiResponse<TableBookingResponseDTO> extend(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @Valid @RequestBody TableBookingExtendRequestDTO request
    ) {
        return new ApiResponse<>(200, "EXTEND_TABLE_BOOKING_SUCCESS", tableBookingService.extend(bookingId, request));
    }

    @PostMapping("/walk-in")
    public ApiResponse<TableBookingResponseDTO> walkIn(
            @Valid @RequestBody TableBookingCreateRequestDTO request,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        return new ApiResponse<>(200, "CREATE_WALK_IN_BOOKING_SUCCESS", tableBookingService.createWalkIn(request, force));
    }
}
