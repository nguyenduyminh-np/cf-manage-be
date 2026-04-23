package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.payment.PaymentRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequestDTO request);
}
