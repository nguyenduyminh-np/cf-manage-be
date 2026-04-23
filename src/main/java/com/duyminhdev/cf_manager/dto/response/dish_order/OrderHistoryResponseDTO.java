package com.duyminhdev.cf_manager.dto.response.dish_order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderHistoryResponseDTO {
    private Integer dishOrderId;
    private String tableName;
    private String employeeName;
    private String orderStatus;
    private String dishOrderStatusCode;
    private Instant createdAt;
    private String note;
    private Long totalQuantity;      // Số lượng món
    private BigDecimal totalAmount;
}
