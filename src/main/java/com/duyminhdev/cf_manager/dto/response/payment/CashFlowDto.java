package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CashFlowDto {
    private Integer id;
    private BigDecimal totalAmount;
    private String flowType;
    private String note;
    private Instant createdAt;
}