package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishGroupedByTableNativeResultDTO {

    private Integer dishId;
    private String dishCode;
    private String dishName;
    private String dishPhoto;

    private Integer totalQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
