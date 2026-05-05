package com.duyminhdev.cf_manager.dto.response.ingredient_category;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class IngredientCategoryDetailResponseDTO {
    private Integer id;
    private String ingredientCategoryCode;
    private String ingredientCategoryName;
    private Instant createdTime;
    private Boolean active;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
