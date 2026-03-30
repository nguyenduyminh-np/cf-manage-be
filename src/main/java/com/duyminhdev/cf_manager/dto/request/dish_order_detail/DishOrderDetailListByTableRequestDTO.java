package com.duyminhdev.cf_manager.dto.request.dish_order_detail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishOrderDetailListByTableRequestDTO {

    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;
}

