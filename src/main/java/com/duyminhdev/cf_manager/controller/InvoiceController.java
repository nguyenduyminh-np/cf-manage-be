package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.invoice.*;
import com.duyminhdev.cf_manager.dto.response.invoice.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<InvoiceListItemDTO>>> search(
            @Valid @RequestBody InvoiceSearchRequestDTO request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "SUCCESS", invoiceService.search(request));
    }

    @PostMapping("/get-detail")
    public ApiResponse<InvoiceDetailResponseDTO> getDetail(
            @Valid @RequestBody InvoiceDetailRequestDTO request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "SUCCESS", invoiceService.getDetail(request.getInvoiceId()));
    }

    /**
     * Lay thong tin dem hoa don, cho phep body optional.
     */
    @PostMapping("/count")
    public ApiResponse<InvoiceCountResponseDTO> count(
            @RequestBody(required = false) InvoiceCountRequestDTO request
    ) {
        /**
         * Flow API invoice count:
         * 1. Nhan request count (co the null)
         * 2. Neu null thi tao request mac dinh
         * 3. Goi service dem hoa don va sinh ma tiep theo
         * 4. Tra ket qua count cho FE
         */
        InvoiceCountRequestDTO safeRequest = request != null ? request : new InvoiceCountRequestDTO();
        return new ApiResponse<>(200, "GET_INVOICE_COUNT_SUCCESS", invoiceService.count(safeRequest));
    }

    /**
     * Tao hoa don moi cho ban.
     */
    @PostMapping("/create")
    public ApiResponse<InvoiceResponseDTO> create(
            @Valid @RequestBody InvoiceCreateRequestDTO request
    ) {
        /**
         * Flow API invoice create:
         * 1. Nhan du lieu tao hoa don
         * 2. Goi service tao hoa don va chi tiet
         * 3. Tra hoa don vua tao
         */
        return new ApiResponse<>(200, "CREATE_INVOICE_SUCCESS", invoiceService.create(request));
    }

    /**
     * Xac nhan trang thai thanh toan cho hoa don.
     */
    @PostMapping("/confirm-payment")
    public ApiResponse<InvoiceConfirmPaymentResponseDTO> confirmPayment(
            @Valid @RequestBody InvoiceConfirmPaymentRequestDTO request
    ) {
        /**
         * Flow API invoice confirm-payment:
         * 1. Nhan invoiceId va paymentStatus moi
         * 2. Goi service xac nhan thanh toan
         * 3. Tra ket qua trang thai sau cap nhat
         */
        return new ApiResponse<>(200, "CONFIRM_PAYMENT_SUCCESS", invoiceService.confirmPayment(request));
    }

    /**
     * Lay chi tiet day du cua hoa don.
     */
    @PostMapping("/detail")
    public ApiResponse<InvoiceDetailResponse> detail(
            @Valid @RequestBody InvoiceDetailRequestDTO request
    ) {
        /**
         * Flow API invoice detail:
         * 1. Nhan invoiceId can xem chi tiet
         * 2. Goi service lay header + line items
         * 3. Tra du lieu chi tiet cho FE
         */
        return new ApiResponse<>(200, "GET_INVOICE_DETAIL_SUCCESS", invoiceService.detail(request));
    }
}
