package com.duyminhdev.cf_manager.dto.request.ingredient_category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientCategoryIdRequest {
    @NotNull(message = "ID danh mục không được để trống")
    private Integer id;
}
