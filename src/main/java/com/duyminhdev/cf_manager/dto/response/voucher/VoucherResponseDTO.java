package com.duyminhdev.cf_manager.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Response khi query chi tiết / danh sách voucher (admin). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponseDTO {
    private Integer id;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private Instant startDate;
    private Instant endDate;
    private Boolean active;
    /** account_id của người tạo (admin/manager). */
    private Integer createdBy;
    private Instant createdAt;
}
