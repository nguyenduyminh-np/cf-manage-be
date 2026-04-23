package com.duyminhdev.cf_manager.dto.request.dish_order;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class DishOrderCreateRequestDTO {
    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;
    private String description;

    @NotEmpty(message = "Danh sách món ăn không được để rỗng")
    private List<DishOrderDetailPayloadDTO> dishOrderDetails;
}
