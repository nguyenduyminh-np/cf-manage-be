package com.duyminhdev.cf_manager.dto.request.dish_order;


import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DishOrderUpdateRequestDTO {
    @NotNull(message = "Mã đơn gọi món không được để trống")
    @Positive(message = "Mã đơn gọi món phải lớn hơn 0")
    private Integer dishOrderId;

    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái đơn gọi món không hợp lệ")
    private String dishOrderStatus;

    @NotEmpty(message = "Danh sách món ăn không được để rỗng")
    @Valid
    private List<DishOrderDetailPayloadDTO> dishOrderDetails;
}
