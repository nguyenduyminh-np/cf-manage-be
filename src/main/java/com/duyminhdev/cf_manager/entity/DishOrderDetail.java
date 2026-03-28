package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dish_order_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DishOrderDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "price", nullable = false, precision = 18, scale = 0)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_order_detail_dish_order_id"))
    private DishOrder dishOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_order_detail_dish_id"))
    private Dish dish;
}
