package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.dish_order.PaymentPreviewRequestDTO;
import com.duyminhdev.cf_manager.dto.request.payment.PaymentRequestDTO;
import com.duyminhdev.cf_manager.dto.request.payment_method.PaymentMethodListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment.PaymentPreviewResponseDTO;
import com.duyminhdev.cf_manager.dto.response.payment.PaymentResponse;
import com.duyminhdev.cf_manager.dto.response.payment_method.PaymentMethodResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.DishOrderService;
import com.duyminhdev.cf_manager.service.PaymentMethodService;
import com.duyminhdev.cf_manager.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class PaymentController {

    private final PaymentMethodService paymentMethodService;
    private final PaymentService paymentService;
    private final DishOrderService dishOrderService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<PaymentPreviewResponseDTO>> getPaymentPreview(
            @Valid @RequestBody PaymentPreviewRequestDTO request) {
        PaymentPreviewResponseDTO data = dishOrderService.getPaymentPreview(request.getOrderId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", data));
    }

    @PostMapping("/thanh-toan")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        PaymentResponse data = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Thanh toán thành công", data));
    }

    /**
     * Lay danh sach phuong thuc thanh toan, cho phep body optional.
     */
    @PostMapping("/phuong-thuc-thanh-toan")
    public ApiResponse<List<PaymentMethodResponseDTO>> list(
            @RequestBody(required = false) PaymentMethodListRequestDTO request
    ) {
        /**
         * Flow API payment-method list:
         * 1. Nhan request list payment method (co the null)
         * 2. Neu null thi tao request mac dinh
         * 3. Goi service lay danh sach method
         * 4. Tra ket qua cho FE
         */
        PaymentMethodListRequestDTO safeRequest = request != null ? request : new PaymentMethodListRequestDTO();
        return new ApiResponse<>(200, "GET_PAYMENT_METHOD_LIST_SUCCESS", paymentMethodService.getAll(safeRequest));
    }
}
