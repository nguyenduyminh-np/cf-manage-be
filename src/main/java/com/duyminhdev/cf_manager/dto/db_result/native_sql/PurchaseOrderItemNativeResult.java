// PurchaseOrderItemNativeResult.java
package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class PurchaseOrderItemNativeResult {
    private Integer detailId;
    private Integer ingredientId;
    private String ingredientName;
    private Integer quantity;
    private BigDecimal unitPrice;
}