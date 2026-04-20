package com.duyminhdev.cf_manager.dto.request.dish_order;


import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishOrderStatusUpdateRequestDTO {
    @NotNull(message = "Mã đơn gọi món không được để trống")
    @Positive(message = "Mã đơn gọi món phải lớn hơn 0")
    private Integer dishOrderId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái đơn gọi món không hợp lệ")
    private String dishOrderStatus;
}
