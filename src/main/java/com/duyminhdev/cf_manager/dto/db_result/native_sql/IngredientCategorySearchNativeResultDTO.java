package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class IngredientCategorySearchNativeResultDTO {
    private Integer id;
    private String ingredientCategoryCode;
    private String ingredientCategoryName;
    private Instant createdTime;
    private Boolean active;
    private Integer parentCategoryId;
    private String parentCategoryName;
    private Integer ingredientCount;
}
