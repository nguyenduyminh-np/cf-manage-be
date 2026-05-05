package com.duyminhdev.cf_manager.dto.request.warehouse;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class WarehouseSearchRequestDTO {
    private String searchString;   // tìm theo mã hoặc tên
    private Boolean active;        // null -> tất cả, true/false -> lọc
    private String sortField;      // id, warehouseCode, warehouseName, location, createdTime, ingredientCount
    private String sortDir;        // ASC / DESC
    @Min(0) private Integer page = 0;
    @Min(1) private Integer limit = 25;
}
