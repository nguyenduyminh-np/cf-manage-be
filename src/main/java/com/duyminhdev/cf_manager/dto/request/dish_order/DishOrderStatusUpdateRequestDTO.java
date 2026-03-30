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
    @NotNull(message = "dishOrderId is required")
    @Positive(message = "dishOrderId must be > 0")
    private Integer dishOrderId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid dishOrderStatus code")
    private String dishOrderStatus;
}
