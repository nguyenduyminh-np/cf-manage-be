package com.duyminhdev.cf_manager.dto.response.dish_category;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishCategoryResponseDTO {

    private Integer dishCategoryId;
    private String dishCategoryCode;
    private String dishCategoryName;

    private Boolean active;
    private Instant createdTime;
}

