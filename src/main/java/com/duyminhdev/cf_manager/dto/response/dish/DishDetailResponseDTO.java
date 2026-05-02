package com.duyminhdev.cf_manager.dto.response.dish;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class DishDetailResponseDTO {
    private Integer id;
    private String dishCode;
    private String dishName;
    private BigDecimal price;
    private String photo;
    private Instant createdTime;
    private Boolean active;
    private Integer dishCategoryId;
    private String dishCategoryName;       // tên danh mục
}