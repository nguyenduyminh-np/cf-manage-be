package com.duyminhdev.cf_manager.dto.request.ingredient_category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientCategoryUpdateRequestDTO {
    @NotNull(message = "ID danh mục không được để trống")
    private Integer id;
    private String ingredientCategoryCode;
    private String ingredientCategoryName;
    private Integer parentCategoryId;
    private Boolean active;
}
