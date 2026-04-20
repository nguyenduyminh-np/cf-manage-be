package com.duyminhdev.cf_manager.dto.request.table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TableDetailRequestDTO {
    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn không hợp lệ")
    private Integer tableId;
}
