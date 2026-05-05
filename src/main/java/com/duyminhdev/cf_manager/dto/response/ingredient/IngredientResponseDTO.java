// IngredientResponseDTO.java (dành cho danh sách/search)
package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class IngredientResponseDTO {
    private Integer id;
    private String ingredientCode;
    private String ingredientName;
    private Integer selfLife;
    private BigDecimal averagePrice;
    private Instant createdTime;
    private Boolean active;
    private Integer ingredientCategoryId;
    private String ingredientCategoryName;
    private Integer supplierId;
    private String supplierName;
    private Integer unitId;
    private String unitName;
    private Integer currentStock;        // tổng tồn kho khả dụng
}