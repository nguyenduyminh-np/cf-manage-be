package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.payment.OrderAndPayRequestDTO;
import com.duyminhdev.cf_manager.dto.request.payment.PaymentRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequestDTO request);

    /**
     * Tạo đơn order và thực hiện thanh toán ngay lập tức trong một transaction duy nhất.
     * Dùng cho luồng POS "Đặt món &amp; Thanh toán ngay" — bỏ qua các bước trung gian:
     * lưu order → đổi trạng thái → thanh toán.
     */
    PaymentResponse orderAndPay(OrderAndPayRequestDTO request);
}
