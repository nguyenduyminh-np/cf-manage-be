package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Kết quả native query nguyên liệu sắp hết hoặc sắp hết hạn.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientStockDTO {

    private String  ingredientName;

    /** Tổng số lượng còn trong kho (tất cả batch) */
    private Integer totalQuantity;

    /** Tên đơn vị tính (kg, lít, gói...) */
    private String  unitName;

    /** Ngày hết hạn gần nhất của batch còn tồn */
    private Instant nearestExpirationAt;

    /** true = sắp hết hạn (trong 7 ngày), false = sắp hết số lượng */
    private Boolean isNearExpiry;
}
