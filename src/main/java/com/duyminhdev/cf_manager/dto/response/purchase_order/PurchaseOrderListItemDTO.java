// PurchaseOrderListItemDTO.java
package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PurchaseOrderListItemDTO {
    private Integer id;
    private String purchaseOrderCode;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String accountFullName;
    private String supplierName;    // Fix #1
    private Instant createdTime;
    private Instant orderDate;
}