package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
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
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingExportDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.TableBookingService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/table-booking")
@RequiredArgsConstructor
@Validated
@AdminOrManagerAccess
public class TableBookingController {

    private final TableBookingService tableBookingService;

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

    @PostMapping("/pending-job")
    public ApiResponse<List<TableBookingResponseDTO>> getPendingAndConfirmedBookings(@Valid @RequestBody TableBookingSearchRequestDTO request) {
        List<TableBookingResponseDTO> data = tableBookingService.getPendingAndConfirmedBookings(request);
        return new ApiResponse<>(200, "GET_PENDING_JOB_SUCCESS", data);
    }

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

    @PostMapping("table-booking-history/export")
    public void exportExcel(@Valid @RequestBody TableBookingSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<TableBookingExportDTO> items = tableBookingService.exportData(request);

        String fileName = "DANH_SACH_DAT_BAN_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH ĐẶT BÀN";

        ExcelUtils.export(response, TableBookingExportDTO.class, items, fileName, title);
    }

    /**
     * Nghiep vu: Lay chi tiet day du cua mot booking.
     * Rule: bookingId phai > 0 va booking phai ton tai.
     * Chuc nang: Goi service detail va tra du lieu chi tiet cho FE.
     */
    @PostMapping("/detail")
    public ApiResponse<TableBookingResponseDTO> detail(
            @Valid @RequestBody TableBookingDetailRequestDTO request
    ) {
        return new ApiResponse<>(200, "GET_TABLE_BOOKING_DETAIL_SUCCESS", tableBookingService.detail(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(
            @Valid @RequestBody TableBookingConfirmRequestDTO request
    ) {
        tableBookingService.delete(request.getBookingId());
        return new ApiResponse<>(200, "DELETE_TABLE_BOOKING_SUCCESS");
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
    @PostMapping("/confirm")
    public ApiResponse<TableBookingResponseDTO> confirm(
            @Valid @RequestBody TableBookingConfirmRequestDTO request
    ) {
        return new ApiResponse<>(200, "CONFIRM_TABLE_BOOKING_SUCCESS", tableBookingService.confirm(request));
    }

    /**
     * Nghiep vu: Check-in booking khi khach den ban.
     * Rule: bookingId phai > 0 va cho phep force khi can override.
     * Chuc nang: Goi service checkIn va tra booking da check-in.
     */
    @PostMapping("/check-in")
    public ApiResponse<TableBookingResponseDTO> checkIn(
            @Valid @RequestBody TableBookingCheckInRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_IN_TABLE_BOOKING_SUCCESS", tableBookingService.checkIn(request));
    }

    /**
     * Nghiep vu: Check-out booking khi khach roi ban.
     * Rule: request hop le, bookingId > 0 va booking dang o trang thai cho phep checkout.
     * Chuc nang: Goi service checkOut va tra booking da checkout.
     */
    @PostMapping("/check-out")
    public ApiResponse<TableBookingResponseDTO> checkOut(
            @Valid @RequestBody TableBookingCheckOutRequestDTO request
    ) {
        return new ApiResponse<>(200, "CHECK_OUT_TABLE_BOOKING_SUCCESS", tableBookingService.checkOut(request));
    }

    /**
     * Nghiep vu: Huy booking theo yeu cau van hanh.
     * Rule: bookingId phai > 0 va khong huy booking da hoan tat.
     * Chuc nang: Goi service cancel va tra booking da huy.
     */
    @PostMapping("/cancel")
    public ApiResponse<TableBookingResponseDTO> cancel(
            @Valid @RequestBody TableBookingCancelRequestDTO request
    ) {
        return new ApiResponse<>(200, "CANCEL_TABLE_BOOKING_SUCCESS", tableBookingService.cancel(request));
    }

    /**
     * Nghiep vu: Ghi nhan thanh toan coc cho booking.
     * Rule: bookingId phai > 0 va depositAmount phai > 0.
     * Chuc nang: Goi service deposit va tra booking sau khi cap nhat coc.
     */
    @PostMapping("/deposit")
    public ApiResponse<TableBookingResponseDTO> deposit(
            @Valid @RequestBody TableBookingDepositRequestDTO request
    ) {
        return new ApiResponse<>(200, "DEPOSIT_TABLE_BOOKING_SUCCESS", tableBookingService.deposit(request));
    }

    /**
     * Nghiep vu: Gia han thoi gian su dung ban cua booking.
     * Rule: bookingId phai > 0 va expectedCheckOut moi phai hop le.
     * Chuc nang: Goi service extend va tra booking sau khi gia han.
     */
    @PostMapping("/extend")
    public ApiResponse<TableBookingResponseDTO> extend(
            @Valid @RequestBody TableBookingExtendRequestDTO request
    ) {
        return new ApiResponse<>(200, "EXTEND_TABLE_BOOKING_SUCCESS", tableBookingService.extend(request));
    }

    @PostMapping("/tables/available-slots")
    public ApiResponse<List<TableBookingAvailableSlotResponseDTO>> availableSlots(
            @Valid @RequestBody TableBookingAvailableSlotsRequestDTO request
    ) {
        return new ApiResponse<>(200, "GET_AVAILABLE_SLOTS_SUCCESS", tableBookingService.getAvailableSlots(request));
    }

    /**
     * Nghiep vu: Tao booking walk-in cho khach den truc tiep.
     * Rule: Request tao booking hop le va cho phep force khi can.
     * Chuc nang: Goi service createWalkIn va tra booking walk-in.
         * Legacy: Walk-in thuần nên dùng API /create với isWalkIn=true.
         * Endpoint nay duoc giu de tuong thich nguoc va cho flow lateBookingId.
     */
        @Deprecated
    @PostMapping("/walk-in")
    public ApiResponse<TableBookingResponseDTO> walkIn(
            @Valid @RequestBody(required = false) TableBookingWalkInRequestDTO request,
            @RequestParam(value = "lateBookingId", required = false) @Positive(message = "lateBookingId must be > 0") Integer lateBookingId,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        if (lateBookingId != null) {
            return new ApiResponse<>(
                    200,
                    "CREATE_LATE_ARRIVAL_WALK_IN_SUCCESS",
                    tableBookingService.createWalkInFromLateArrival(lateBookingId, request, force)
            );
        }

        return new ApiResponse<>(200, "CREATE_WALK_IN_BOOKING_SUCCESS", tableBookingService.createWalkIn(request, force));
    }
}
