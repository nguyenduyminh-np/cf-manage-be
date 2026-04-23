package com.duyminhdev.cf_manager.dto.request.dish_order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPreviewRequestDTO {

    @NotNull(message = "Mã đơn đặt món không được để trống")
    @Positive(message = "Mã đơn đặt món phải lớn hơn 0")
    private Integer orderId;
}
