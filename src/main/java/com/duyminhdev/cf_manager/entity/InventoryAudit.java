package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_audit")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "audit_code", length = 255)
    private String auditCode;

    @Column(name = "audit_at", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "auditor", nullable = false, length = 255)
    private String auditor;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_audit_warehouse_id"))
    private Warehouse warehouse;
}
