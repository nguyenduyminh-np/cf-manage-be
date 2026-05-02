package com.duyminhdev.cf_manager.dto.request.dish_category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DishCategoryCreateRequestDTO {
    private String dishCategoryCode;   // optional

    @NotBlank(message = "Tên danh mục không được để trống")
    private String dishCategoryName;
}