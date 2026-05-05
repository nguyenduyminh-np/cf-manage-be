package com.duyminhdev.cf_manager.dto.request.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseIdRequest {
    @NotNull(message = "ID kho không được để trống")
    private Integer id;
}
