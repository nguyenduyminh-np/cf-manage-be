package com.duyminhdev.cf_manager.dto.request.dish;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishCreateRequestDTO {
    private String dishCode;                     // optional

    @NotBlank(message = "Tên món không được trống")
    private String dishName;

    @NotNull(message = "Giá món không được trống")
    private BigDecimal price;

    @NotBlank(message = "Ảnh món không được trống")
    private String photo;

    @NotNull(message = "Danh mục món không được trống")
    private Integer dishCategoryId;
}