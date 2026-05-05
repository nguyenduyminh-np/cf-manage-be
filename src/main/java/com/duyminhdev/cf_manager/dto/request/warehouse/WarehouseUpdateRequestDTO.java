package com.duyminhdev.cf_manager.dto.request.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseUpdateRequestDTO {
    @NotNull(message = "ID kho không được để trống")
    private Integer id;
    private String warehouseCode;
    private String warehouseName;
    private String location;
    private String note;
    private Boolean active;
}
