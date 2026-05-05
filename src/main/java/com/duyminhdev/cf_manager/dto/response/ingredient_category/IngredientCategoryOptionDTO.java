package com.duyminhdev.cf_manager.dto.response.ingredient_category;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class IngredientCategoryOptionDTO {
    private Integer id;
    private String ingredientCategoryCode;
    private String ingredientCategoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
