package com.duyminhdev.cf_manager.dto.request.dish_order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DishOrderListByTableRequestDTO {
    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;
}
