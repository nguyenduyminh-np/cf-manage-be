// IngredientDetailResponseDTO.java (chi tiết + danh sách lô tồn kho)
package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @Builder
public class IngredientDetailResponseDTO {
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
    private List<StockLevelDTO> stockLevels;   // chi tiết các lô
}