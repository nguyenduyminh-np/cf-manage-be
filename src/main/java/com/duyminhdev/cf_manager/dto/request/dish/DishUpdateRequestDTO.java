package com.duyminhdev.cf_manager.dto.request.dish;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishUpdateRequestDTO {
    @NotNull(message = "Id món ăn không được trống")
    private Integer id;

    private String dishCode;
    private String dishName;
    private BigDecimal price;
    private String photo;
    private Integer dishCategoryId;
    private Boolean active;        // nếu null giữ nguyên
}