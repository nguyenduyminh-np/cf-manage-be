package com.duyminhdev.cf_manager.dto.request.ingredient_category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientCategoryCreateRequestDTO {
    private String ingredientCategoryCode;     // có thể null
    @NotBlank(message = "Tên danh mục không được để trống")
    private String ingredientCategoryName;
    private Integer parentCategoryId;          // có thể null -> danh mục gốc
}
