package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class InvoiceDetailDto {
    private Integer id;
    private Integer dishId;
    private String dishName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}