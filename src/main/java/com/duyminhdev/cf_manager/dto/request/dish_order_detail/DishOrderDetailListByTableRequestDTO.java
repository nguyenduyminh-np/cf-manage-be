package com.duyminhdev.cf_manager.dto.request.dish_order_detail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishOrderDetailListByTableRequestDTO {

    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;
}

