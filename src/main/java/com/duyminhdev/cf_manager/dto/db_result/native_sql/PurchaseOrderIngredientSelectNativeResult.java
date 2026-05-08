package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderIngredientSelectNativeResult {
    private Integer ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private Integer supplierId;
}
