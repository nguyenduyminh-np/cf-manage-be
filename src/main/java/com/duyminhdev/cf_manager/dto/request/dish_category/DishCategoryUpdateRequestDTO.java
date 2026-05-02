package com.duyminhdev.cf_manager.dto.request.dish_category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DishCategoryUpdateRequestDTO {
    @NotNull(message = "Id danh mục không được để trống")
    private Integer id;

    private String dishCategoryCode;
    private String dishCategoryName;
    private Boolean active;        // nếu không truyền -> giữ nguyên
}