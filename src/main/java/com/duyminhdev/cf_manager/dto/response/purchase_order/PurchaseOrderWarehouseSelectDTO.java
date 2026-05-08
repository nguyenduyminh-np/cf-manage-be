package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderWarehouseSelectDTO {
    private Integer id;
    private String warehouseCode;
    private String warehouseName;
}
