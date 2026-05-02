package com.duyminhdev.cf_manager.dto.request.dish_category;


import lombok.Data;

import java.time.Instant;

@Data
public class DishCategorySearchRequestDTO {
    private String dishCategoryCode;      // tìm kiếm gần đúng (LIKE)
    private String dishCategoryName;      // tìm kiếm gần đúng (LIKE)
    private Instant fromDate;             // created_at >=
    private Instant toDate;               // created_at <=
    private Boolean isActive;             // lọc chính xác (nếu null -> lấy tất cả)
    private Integer page;                 // 0‑based
    private Integer limit;
    private String sortField;             // mặc định "createdTime"
    private String sortDir;               // asc/desc
}