package com.duyminhdev.cf_manager.dto.request.dish_order_detail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DishOrderDetailListByOrderRequestDTO {

    @NotNull(message = "Mã đơn gọi món không được để trống")
    @Positive(message = "Mã đơn gọi món phải lớn hơn 0")
    private Integer dishOrderId;
}
