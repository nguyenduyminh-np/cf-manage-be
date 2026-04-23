package com.duyminhdev.cf_manager.dto.response.dish_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOrderDetailsDTO {
    private Integer dishOrderDetailId;
    private Integer dishOrderId;
    private Integer dishId;
    private String dishName;
    private String photo;
    private Integer quantity;
    private String note;
    private BigDecimal unitPrice; // đơn giá gốc của món
    private BigDecimal totalPrice; // Tính toán từ quantity của order * đơn giá gốc của món
}
