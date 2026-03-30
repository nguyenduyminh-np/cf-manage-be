package com.duyminhdev.cf_manager.dto.request.dish_order_detail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DishOrderDetailListByOrderRequestDTO {

    @NotNull(message = "dishOrderId is required")
    @Positive(message = "dishOrderId must be > 0")
    private Integer dishOrderId;
}
