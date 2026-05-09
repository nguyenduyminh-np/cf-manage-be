package com.duyminhdev.cf_manager.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Kết quả sau khi apply voucher thành công (bước thanh toán, đã ghi DB).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppliedVoucherDTO {
    private Integer voucherId;
    private String voucherCode;
    private BigDecimal discountAmount;  // Số tiền thực tế được giảm
    private BigDecimal totalAmount;     // Tổng tiền gốc
    private BigDecimal finalAmount;     // Tổng tiền sau giảm
}
