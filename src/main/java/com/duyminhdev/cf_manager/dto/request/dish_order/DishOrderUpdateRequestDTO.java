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
    @NotNull(message = "dishOrderId is required")
    @Positive(message = "dishOrderId must be > 0")
    private Integer dishOrderId;

    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid dishOrderStatus code")
    private String dishOrderStatus;

    @NotEmpty(message = "dishOrderDetails must not be empty")
    @Valid
    private List<DishOrderDetailPayloadDTO> dishOrderDetails;
}
