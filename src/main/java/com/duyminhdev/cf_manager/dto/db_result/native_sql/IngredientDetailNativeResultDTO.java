package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Native SQL result DTO cho query chi tiết 1 nguyên liệu (có JOIN sang
 * ingredient_category, supplier, unit để lấy thêm code + name).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDetailNativeResultDTO {
    private Integer id;
    private String ingredientCode;
    private String ingredientName;
    private Integer selfLife;
    private BigDecimal averagePrice;
    private Instant createdTime;
    private Boolean active;

    // ingredient_category
    private Integer ingredientCategoryId;
    private String ingredientCategoryCode;
    private String ingredientCategoryName;

    // supplier
    private Integer supplierId;
    private String supplierCode;
    private String supplierName;

    // unit
    private Integer unitId;
    private String unitCode;
    private String unitName;
}
