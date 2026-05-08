// PurchaseOrderDetailNativeResult.java
package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class PurchaseOrderDetailNativeResult {
    private Integer id;
    private String purchaseOrderCode;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private Instant orderDate;
    private Instant createdTime;
    private Integer accountId;
    private String fullName;
    private Integer supplierId;
    private String supplierName;    // Fix #1
    private Integer warehouseId;
    private String warehouseName;   // Fix #1
}
