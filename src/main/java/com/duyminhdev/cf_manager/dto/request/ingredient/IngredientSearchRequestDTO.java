// IngredientSearchRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.ingredient;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class IngredientSearchRequestDTO {
    private String searchString;      // tìm theo mã hoặc tên
    private Integer ingredientCategoryId;
    private Integer supplierId;
    private Boolean active;           // null -> tất cả, true/false lọc trạng thái
    private String sortField;         // id, ingredientCode, ingredientName, etc.
    private String sortDir;           // ASC/DESC
    @Min(0) private Integer page = 0;
    @Min(1) private Integer limit = 25;
}