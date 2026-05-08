// PurchaseOrderDetailResponseDTO.java
package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PurchaseOrderDetailResponseDTO {
    private Integer id;
    private String purchaseOrderCode;
    private BigDecimal totalPrice;
    private String paymentStatus;      // code: DRAFT, PENDING, APPROVED, COMPLETED, CANCELLED
    private String paymentStatusName;  // label: Bản nháp, Chờ duyệt, ...
    private String accountFullName;
    private Integer supplierId;
    private String supplierName;    // Fix #1
    private Integer warehouseId;
    private String warehouseName;   // Fix #1
    private Instant createdTime;
    private Instant orderDate;
    private List<DetailItem> details;

    @Data
    @Builder
    public static class DetailItem {
        private Integer id;
        private Integer ingredientId;
        private String ingredientCode;
        private String ingredientName;
        private Integer supplierId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
