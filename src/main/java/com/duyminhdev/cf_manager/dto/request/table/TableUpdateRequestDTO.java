package com.duyminhdev.cf_manager.dto.request.table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TableUpdateRequestDTO {

    @NotNull(message = "Mã bàn không được trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer id;

    private String tableCode;
    private String tableName;

    @Positive(message = "Tầng phải lớn hơn 0")
    private Integer floor;

    @Positive(message = "Số chỗ phải lớn hơn 0")
    private Integer slot;

    private String tableStatus;
    private Boolean active;
}