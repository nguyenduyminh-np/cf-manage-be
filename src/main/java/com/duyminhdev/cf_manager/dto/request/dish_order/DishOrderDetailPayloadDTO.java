package com.duyminhdev.cf_manager.dto.request.dish_order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DishOrderDetailPayloadDTO {
    @Positive(message = "detailId must be > 0")
    private Integer detailId;

    @NotNull(message = "dishId is required")
    @Positive(message = "dishId must be > 0")
    private Integer dishId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be > 0")
    private Integer quantity;

    @Size(max = 500, message = "note must be <= 500 characters")
    private String note;
}
