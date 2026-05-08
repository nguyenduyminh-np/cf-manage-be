package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderIngredientSelectDTO {
    private Integer ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private Integer supplierId;
}
