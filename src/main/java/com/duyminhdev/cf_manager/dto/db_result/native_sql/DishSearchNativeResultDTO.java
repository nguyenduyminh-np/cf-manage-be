package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class DishSearchNativeResultDTO {
    private Long id;
    private String dishCode;
    private String dishName;
    private BigDecimal price;
    private String photo;
    private Instant createdAt;
    private Long dishCategoryId;
    private String dishCategoryName;
}