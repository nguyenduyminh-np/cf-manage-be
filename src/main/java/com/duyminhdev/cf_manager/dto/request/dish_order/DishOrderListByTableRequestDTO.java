package com.duyminhdev.cf_manager.dto.request.dish_order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DishOrderListByTableRequestDTO {
    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;
}
