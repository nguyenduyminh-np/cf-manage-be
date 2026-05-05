package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;

/**
 * DTO dùng làm nguồn dữ liệu cho select/dropdown danh mục nguyên liệu
 * khi thêm mới / cập nhật nguyên liệu.
 */
@Data
@Builder
public class IngredientCategorySelectDTO {
    private String ingredientCategoryCode;
    private String ingredientCategoryName;
}
