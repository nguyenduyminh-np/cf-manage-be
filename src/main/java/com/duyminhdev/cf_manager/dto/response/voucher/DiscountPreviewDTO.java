package com.duyminhdev.cf_manager.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Kết quả khi preview voucher (bước xem trước, không thay đổi DB).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountPreviewDTO {
    private String voucherCode;
    private String discountType;        // "PERCENT" hoặc "FIXED"
    private BigDecimal discountValue;   // Giá trị gốc (% hoặc số tiền)
    private BigDecimal discountAmount;  // Số tiền thực tế được giảm
    private BigDecimal totalAmount;     // Tổng tiền gốc
    private BigDecimal finalAmount;     // Tổng tiền sau giảm = totalAmount - discountAmount
}
