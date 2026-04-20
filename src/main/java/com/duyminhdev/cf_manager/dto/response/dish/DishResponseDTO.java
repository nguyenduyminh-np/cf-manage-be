package com.duyminhdev.cf_manager.dto.response.dish;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishResponseDTO {

    private Integer dishId;
    private String dishCode;
    private String dishName;
    private BigDecimal price;
    private String photo;

    private Integer dishCategoryId;
    private String dishCategoryCode;
    private String dishCategoryName;

    private Boolean active;
    private Instant createdTime;
}

