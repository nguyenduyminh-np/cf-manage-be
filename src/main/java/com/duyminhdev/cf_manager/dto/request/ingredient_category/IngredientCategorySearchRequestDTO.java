package com.duyminhdev.cf_manager.dto.request.ingredient_category;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class IngredientCategorySearchRequestDTO {
    private String searchString;      // tìm theo mã hoặc tên
    private Boolean active;           // null -> tất cả, true/false -> lọc
    private Integer parentCategoryId; // null -> không lọc
    private String sortField;         // id, ingredientCategoryCode, ingredientCategoryName, createdTime, active, parentCategoryName, ingredientCount
    private String sortDir;           // ASC / DESC
    @Min(0) private Integer page = 0;
    @Min(1) private Integer limit = 25;
}
