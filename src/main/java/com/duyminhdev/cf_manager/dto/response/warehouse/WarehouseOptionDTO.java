package com.duyminhdev.cf_manager.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WarehouseOptionDTO {
    private Integer id;
    private String warehouseCode;
    private String warehouseName;
}
