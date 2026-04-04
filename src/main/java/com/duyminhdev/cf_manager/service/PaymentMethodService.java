package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.payment_method.PaymentMethodListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment_method.PaymentMethodResponseDTO;

import java.util.List;

public interface PaymentMethodService {

    /**
     * Lấy danh sách phương thức thanh toán khả dụng để hiển thị cho người dùng.
     */
    List<PaymentMethodResponseDTO> getAll(PaymentMethodListRequestDTO request);
}
