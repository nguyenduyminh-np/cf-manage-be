package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrderDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 0)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchase_order_detail_purchase_order_id"))
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchase_order_detail_ingredient_id"))
    private Ingredient ingredient;
}
