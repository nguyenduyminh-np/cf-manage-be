package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderSupplierSelectDTO {
    private Integer id;
    private String supplierCode;
    private String supplierName;
}
