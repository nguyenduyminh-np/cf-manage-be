package com.duyminhdev.cf_manager.dto.request.dish;

import lombok.Data;

@Data
public class DishListRequestDTO {
    private Boolean active = true;
    private Integer dishCategoryId;
}
