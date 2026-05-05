package com.duyminhdev.cf_manager.dto.request.purchase_order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PurchaseOrderSearchRequestDTO {
    private String purchaseOrderCode;
    private String paymentStatus;
    private BigDecimal totalPriceFrom;
    private BigDecimal totalPriceTo;
    private Instant fromDate;
    private Instant toDate;
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}