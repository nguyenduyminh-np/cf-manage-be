package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dish")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Dish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dish_code", length = 255)
    private String dishCode;

    @Column(name = "dish_name", nullable = false, length = 100)
    private String dishName;

    @Column(name = "price", nullable = false, precision = 18, scale = 0)
    private BigDecimal price;

    @Column(name = "photo", nullable = false, length = 255)
    private String photo;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_dish_category_id"))
    private DishCategory dishCategory;
}
