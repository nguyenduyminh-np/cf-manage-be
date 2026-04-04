package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceConfirmPaymentRequestDTO;
import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceCountRequestDTO;
import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceConfirmPaymentResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceCountResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceResponseDTO;

public interface InvoiceService {

    /**
     * Đếm số invoice theo ngày và trả về mã hóa đơn kế tiếp.
     */
    InvoiceCountResponseDTO count(InvoiceCountRequestDTO request);

    /**
     * Tạo hóa đơn mới cho bàn và lưu chi tiết hóa đơn.
     */
    InvoiceResponseDTO create(InvoiceCreateRequestDTO request);

    /**
     * Xác nhận trạng thái thanh toán của hóa đơn.
     */
    InvoiceConfirmPaymentResponseDTO confirmPayment(InvoiceConfirmPaymentRequestDTO request);

    /**
     * Lấy chi tiết đầy đủ của hóa đơn để hiển thị chứng từ thanh toán.
     */
    InvoiceDetailResponseDTO detail(InvoiceDetailRequestDTO request);
}