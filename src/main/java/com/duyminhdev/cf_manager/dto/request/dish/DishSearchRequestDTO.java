package com.duyminhdev.cf_manager.dto.request.dish;

import com.duyminhdev.cf_manager.dto.base.PageFilterRequest;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DishSearchRequestDTO extends PageFilterRequest {
    private String keyword;

    @Positive(message = "dishCategoryId must be > 0")
    private Integer dishCategoryId;

    private Boolean active;
}
