package com.duyminhdev.cf_manager.dto.response.dish_category;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class DishCategoryDetailResponseDTO {
    private Integer id;
    private String dishCategoryCode;
    private String dishCategoryName;
    private Instant createdTime;
    private Boolean active;
    private long dishCount;   // số món đang active trong danh mục (tuỳ chọn)
}