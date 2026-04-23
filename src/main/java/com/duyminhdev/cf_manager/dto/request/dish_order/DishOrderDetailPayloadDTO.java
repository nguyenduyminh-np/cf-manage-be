package com.duyminhdev.cf_manager.dto.request.dish_order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DishOrderDetailPayloadDTO {
    @NotNull(message = "Mã món ăn không được để trống")
    @Positive(message = "Mã món ăn phải lớn hơn 0")
    private Integer dishId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @Size(max = 500, message = "Ghi chú món ăn không được vượt quá 500 ký tự")
    private String note;
}
