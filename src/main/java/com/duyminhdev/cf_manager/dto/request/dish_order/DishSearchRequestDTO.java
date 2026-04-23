package com.duyminhdev.cf_manager.dto.request.dish_order;

import lombok.Data;

@Data
public class DishSearchRequestDTO {
    private String searchString;
    private Long dishCategoryId;  
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}
