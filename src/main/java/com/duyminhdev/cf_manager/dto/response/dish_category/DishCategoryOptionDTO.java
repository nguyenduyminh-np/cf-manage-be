package com.duyminhdev.cf_manager.dto.response.dish_category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DishCategoryOptionDTO {
    private Integer id;
    private String dishCategoryCode;
    private String dishCategoryName;
}