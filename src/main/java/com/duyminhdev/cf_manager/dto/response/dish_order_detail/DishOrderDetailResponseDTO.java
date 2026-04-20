package com.duyminhdev.cf_manager.dto.response.dish_order_detail;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishOrderDetailResponseDTO {

    private Integer dishOrderDetailId;
    private Integer dishOrderId;

    private Integer dishId;
    private String dishCode;
    private String dishName;
    private String dishPhoto;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;

    private String note;
    private Boolean active;
    private Instant createdTime;
}

