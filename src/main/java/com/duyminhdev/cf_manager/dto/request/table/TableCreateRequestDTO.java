package com.duyminhdev.cf_manager.dto.request.table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TableCreateRequestDTO {

    private String tableCode;

    @NotBlank(message = "Tên bàn không được trống")
    private String tableName;

    @Positive(message = "Tầng phải lớn hơn 0")
    private Integer floor;

    @Positive(message = "Số chỗ phải lớn hơn 0")
    private Integer slot;

    private String tableStatus;
}