package com.duyminhdev.cf_manager.dto.response.ingredient_category;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.Data;

@Data
public class IngredientCategoryExportDTO {
    @ExcelColumn(value = "Mã danh mục") private String ingredientCategoryCode;
    @ExcelColumn(value = "Tên danh mục") private String ingredientCategoryName;
    @ExcelColumn(value = "Danh mục cha") private String parentCategoryName;
    @ExcelColumn(value = "Trạng thái") private String active;
}
