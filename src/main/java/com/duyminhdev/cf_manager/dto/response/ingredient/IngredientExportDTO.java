// IngredientExportDTO.java
package com.duyminhdev.cf_manager.dto.response.ingredient;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.Data;

@Data
public class IngredientExportDTO {
    @ExcelColumn(value = "Mã NVL") private String ingredientCode;
    @ExcelColumn(value = "Tên nguyên liệu") private String ingredientName;
    @ExcelColumn(value = "HSD (ngày)") private Integer selfLife;
    @ExcelColumn(value = "Giá TB") private String averagePrice; // có thể format
    @ExcelColumn(value= "Nhóm") private String ingredientCategoryName;
    @ExcelColumn(value = "Nhà cung cấp") private String supplierName;
    @ExcelColumn(value = "Đơn vị") private String unitName;
    @ExcelColumn(value = "Trạng thái") private String active;
}