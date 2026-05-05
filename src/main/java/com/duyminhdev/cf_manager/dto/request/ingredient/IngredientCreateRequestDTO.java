// IngredientCreateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.ingredient;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class IngredientCreateRequestDTO {
    private String ingredientCode;
    @NotBlank(message = "Tên nguyên liệu không được để trống")
    private String ingredientName;
    @NotNull(message = "Hạn sử dụng (ngày) không được để trống")
    @Min(value = 1, message = "Hạn sử dụng phải lớn hơn 0")
    private Integer selfLife;
    @NotNull(message = "Danh mục nguyên liệu không được để trống")
    private Integer ingredientCategoryId;
    @NotNull(message = "Nhà cung cấp không được để trống")
    private Integer supplierId;
    @NotNull(message = "Đơn vị tính không được để trống")
    private Integer unitId;
}