package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDto {
    private Integer dishId;
    private String dishCode;
    private String dishName;
    private Integer quantity;
    private BigDecimal unitPrice; // đơn giá gốc
    private BigDecimal subtotal; // quantity * price
}