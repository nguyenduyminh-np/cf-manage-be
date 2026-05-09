package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPreviewResponseDTO {
    private Integer orderId;
    private Instant orderCreatedAt;
    private DiningTableDto diningTable;
    private AccountDto createdBy;
    private CustomerInfoDto customer;
    private List<OrderItemDto> items;

    // ── Tiền và discount ───────────────────────────────────────────

    /** Tổng tiền gốc trước giảm giá. */
    private BigDecimal totalAmount;

    /** Mã voucher đã áp dụng (null nếu không dùng). */
    private String voucherCode;

    /** Số tiền được giảm (null nếu không dùng voucher). */
    private BigDecimal discountAmount;

    /** Số tiền khách thực trả = totalAmount - discountAmount. */
    private BigDecimal finalAmount;

    private List<String> suggestedPaymentMethods;
}
