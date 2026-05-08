package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bảng 4: Hóa đơn chưa thanh toán (payment_status NOT IN 'PAID', 'ĐÃ HỦY').
 */
@Data
@Builder
public class DashboardPendingInvoiceItemDTO {
    /** Mã hóa đơn. */
    private String invoiceCode;
    /** Tên bàn. */
    private String tableName;
    /** Tổng tiền hóa đơn. */
    private BigDecimal totalAmount;
    /** Thời điểm tạo hóa đơn. */
    private Instant createdAt;
    /** Tên khách hàng (nếu có). */
    private String customerName;
}
