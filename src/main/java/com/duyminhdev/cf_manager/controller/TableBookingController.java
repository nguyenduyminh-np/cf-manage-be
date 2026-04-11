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
     * Nghiep vu: Tra cuu booking theo bo loc va phan trang.
     * Rule: Yeu cau bo loc hop le theo request search.
     * Chuc nang: Goi service search va tra PageResponse.
     */
    @PostMapping("/search")
    public ApiResponse<PageResponse<List<TableBookingResponseDTO>>> search(
            @Valid @RequestBody TableBookingSearchRequestDTO request
    ) {
        return new ApiResponse<>(200, "SEARCH_TABLE_BOOKING_SUCCESS", tableBookingService.search(request));
    }

    /**
     * Nghiep vu: Tao booking moi cho ban.
     * Rule: Khong cho phep tao booking trung khung gio tren ban.
     * Chuc nang: Goi service create va tra booking vua tao.
     */
    @PostMapping("/create")
    public ApiResponse<TableBookingResponseDTO> create(
            @Valid @RequestBody TableBookingCreateRequestDTO request
    ) {
        return new ApiResponse<>(200, "CREATE_TABLE_BOOKING_SUCCESS", tableBookingService.create(request));
    }

    /**
     * Nghiep vu: Cap nhat thong tin booking hien co.
     * Rule: Chi cap nhat booking active va khong o trang thai terminal.
     * Chuc nang: Goi service update va tra booking sau cap nhat.
     */
    @PostMapping("/update")
    public ApiResponse<TableBookingResponseDTO> update(
            @Valid @RequestBody TableBookingUpdateRequestDTO request
    ) {
        return new ApiResponse<>(200, "UPDATE_TABLE_BOOKING_SUCCESS", tableBookingService.update(request));
    }

    /**
     * Nghiep vu: Chuyen trang thai booking theo thao tac quan ly.
     * Rule: Trang thai moi phai hop le theo state machine booking.
     * Chuc nang: Goi service updateStatus va tra ket qua boolean.
     */
    @PostMapping("/update-status")
    public ApiResponse<Boolean> updateStatus(
            @Valid @RequestBody TableBookingStatusUpdateRequestDTO request
    ) {
        return new ApiResponse<>(200, "UPDATE_TABLE_BOOKING_STATUS_SUCCESS", tableBookingService.updateStatus(request));
    }

    /**
     * Nghiep vu: Xac nhan booking truoc gio den cua khach.
     * Rule: bookingId phai > 0 va booking dang cho xac nhan.
     * Chuc nang: Goi service confirm va tra booking da confirm.
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<TableBookingResponseDTO> confirm(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId
    ) {
        return new ApiResponse<>(200, "CONFIRM_TABLE_BOOKING_SUCCESS", tableBookingService.confirm(bookingId));
    }

    /**
     * Nghiep vu: Check-in booking khi khach den ban.
     * Rule: bookingId phai > 0 va cho phep force khi can override.
     * Chuc nang: Goi service checkIn va tra booking da check-in.
     */
    @PostMapping("/{id}/check-in")
    public ApiResponse<TableBookingResponseDTO> checkIn(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @RequestBody(required = false) TableBookingCheckInRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_IN_TABLE_BOOKING_SUCCESS", tableBookingService.checkIn(bookingId, request));
    }

    /**
     * Nghiep vu: Check-out booking khi khach roi ban.
     * Rule: bookingId phai > 0 va booking dang o trang thai cho phep checkout.
     * Chuc nang: Goi service checkOut va tra booking da checkout.
     */
    @PostMapping("/{id}/check-out")
    public ApiResponse<TableBookingResponseDTO> checkOut(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @RequestBody(required = false) TableBookingCheckOutRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_OUT_TABLE_BOOKING_SUCCESS", tableBookingService.checkOut(bookingId, request));
    }

    /**
     * Nghiep vu: Huy booking theo yeu cau van hanh.
     * Rule: bookingId phai > 0 va khong huy booking da hoan tat.
     * Chuc nang: Goi service cancel va tra booking da huy.
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<TableBookingResponseDTO> cancel(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId
    ) {
        return new ApiResponse<>(200, "CANCEL_TABLE_BOOKING_SUCCESS", tableBookingService.cancel(bookingId));
    }

    /**
     * Nghiep vu: Ghi nhan thanh toan coc cho booking.
     * Rule: bookingId phai > 0 va depositAmount phai > 0.
     * Chuc nang: Goi service deposit va tra booking sau khi cap nhat coc.
     */
    @PostMapping("/{id}/deposit")
    public ApiResponse<TableBookingResponseDTO> deposit(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @Valid @RequestBody TableBookingDepositRequestDTO request
    ) {
        return new ApiResponse<>(200, "DEPOSIT_TABLE_BOOKING_SUCCESS", tableBookingService.deposit(bookingId, request));
    }

    /**
     * Nghiep vu: Gia han thoi gian su dung ban cua booking.
     * Rule: bookingId phai > 0 va expectedCheckOut moi phai hop le.
     * Chuc nang: Goi service extend va tra booking sau khi gia han.
     */
    @PostMapping("/{id}/extend")
    public ApiResponse<TableBookingResponseDTO> extend(
            @PathVariable("id") @Positive(message = "bookingId must be > 0") Integer bookingId,
            @Valid @RequestBody TableBookingExtendRequestDTO request
    ) {
        return new ApiResponse<>(200, "EXTEND_TABLE_BOOKING_SUCCESS", tableBookingService.extend(bookingId, request));
    }

    /**
     * Nghiep vu: Tao booking walk-in cho khach den truc tiep.
     * Rule: Request tao booking hop le va cho phep force khi can.
     * Chuc nang: Goi service createWalkIn va tra booking walk-in.
     */
    @PostMapping("/walk-in")
    public ApiResponse<TableBookingResponseDTO> walkIn(
            @Valid @RequestBody TableBookingCreateRequestDTO request,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        return new ApiResponse<>(200, "CREATE_WALK_IN_BOOKING_SUCCESS", tableBookingService.createWalkIn(request, force));
    }
}
