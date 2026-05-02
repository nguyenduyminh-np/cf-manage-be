package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.invoice.*;
import com.duyminhdev.cf_manager.dto.response.invoice.*;

import java.util.List;

public interface InvoiceService {

    PageResponse<List<InvoiceListItemDTO>> search(InvoiceSearchRequestDTO request);
    InvoiceDetailResponseDTO getDetail(Integer invoiceId);
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
    InvoiceDetailResponse detail(InvoiceDetailRequestDTO request);
}