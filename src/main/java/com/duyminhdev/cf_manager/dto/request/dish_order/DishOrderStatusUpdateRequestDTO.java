package com.duyminhdev.cf_manager.dto.request.dish_order;


import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DishOrderStatusUpdateRequestDTO {
    @NotEmpty(message = "Danh sách mã đơn gọi món không được để trống")
    private List<Integer> dishOrderIds;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái đơn gọi món không hợp lệ")
    private String dishOrderStatus;
}
