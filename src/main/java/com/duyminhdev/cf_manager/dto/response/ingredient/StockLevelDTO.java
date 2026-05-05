package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class StockLevelDTO {
    private Integer id;
    private String warehouseName;
    private Integer quantity;
    private Instant expirationDate;
    private BigDecimal unitPrice;
}