package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.payment_method.PaymentMethodListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment_method.PaymentMethodResponseDTO;
import com.duyminhdev.cf_manager.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-method")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    /**
     * Lay danh sach phuong thuc thanh toan, cho phep body optional.
     */
    @PostMapping("/list")
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
