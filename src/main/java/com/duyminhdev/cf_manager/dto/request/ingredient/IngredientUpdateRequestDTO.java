// IngredientUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.ingredient;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class IngredientUpdateRequestDTO {
    @NotNull(message = "ID nguyên liệu không được để trống")
    private Integer id;
    private String ingredientCode;
    private String ingredientName;
    @Min(value = 1, message = "Hạn sử dụng phải lớn hơn 0")
    private Integer selfLife;
    private Integer ingredientCategoryId;
    private Integer supplierId;
    private Integer unitId;
    private Boolean active;
}