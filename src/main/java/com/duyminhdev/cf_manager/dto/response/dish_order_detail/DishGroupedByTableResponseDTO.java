package com.duyminhdev.cf_manager.dto.response.dish_order_detail;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishGroupedByTableResponseDTO {

    private Integer dishId;
    private String dishCode;
    private String dishName;
    private String dishPhoto;

    private Integer totalQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

