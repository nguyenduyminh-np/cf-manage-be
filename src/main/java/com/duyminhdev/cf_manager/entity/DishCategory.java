package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dish_category")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DishCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dish_category_code", length = 255)
    private String dishCategoryCode;

    @Column(name = "dish_category_name", nullable = false, length = 100)
    private String dishCategoryName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;
}
