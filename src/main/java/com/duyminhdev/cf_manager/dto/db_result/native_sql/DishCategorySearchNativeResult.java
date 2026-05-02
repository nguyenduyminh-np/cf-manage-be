package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class DishCategorySearchNativeResult {
    private Integer id;
    private String dishCategoryCode;
    private String dishCategoryName;
    private Instant createdTime;
    private Boolean active;
}