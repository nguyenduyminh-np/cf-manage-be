package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_level")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationDate;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 0)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private Instant lastUpdatedTime = Instant.now();

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_level_ingredient_id"))
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_level_warehouse_id"))
    private Warehouse warehouse;
}
