package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_discrepancy")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryDiscrepancy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "expected_quantity", nullable = false)
    private Integer expectedQuantity;

    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    @Column(name = "discrepancy_reason", length = 255)
    private String discrepancyReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_audit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_discrepancy_inventory_audit_id"))
    private InventoryAudit inventoryAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_level_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_discrepancy_stock_level_id"))
    private StockLevel stockLevel;
}
