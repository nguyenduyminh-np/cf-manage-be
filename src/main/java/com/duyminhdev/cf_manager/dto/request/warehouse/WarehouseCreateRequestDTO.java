package com.duyminhdev.cf_manager.dto.request.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateRequestDTO {
    private String warehouseCode;  // có thể null, nhưng nếu có thì nên duy nhất
    @NotBlank(message = "Tên kho không được để trống")
    private String warehouseName;
    private String location;
    private String note;
}
