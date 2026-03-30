package com.duyminhdev.cf_manager.dto.request.table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TableDetailRequestDTO {
    @NotNull(message = "tableId is required")
    @Positive(message = "Invalid range")
    private Integer tableId;
}
