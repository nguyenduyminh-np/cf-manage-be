package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderHistoryNativeResultDTO {
    private String tableName;
    private String employeeName;
    private String orderStatus;
    private Instant createdAt;
    private String note;
    private Long totalQuantity;      // Số lượng món
    private BigDecimal totalAmount;
}