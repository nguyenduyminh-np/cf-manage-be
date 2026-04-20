package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ingredient_category")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ingredient_category_code", length = 255)
    private String ingredientCategoryCode;

    @Column(name = "ingredient_category_name", nullable = false, length = 100)
    private String ingredientCategoryName;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    // KHÔNG CÓ RÀNG BUỘC FK TRONG DB (Virtual FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private IngredientCategory parentCategory;
}
