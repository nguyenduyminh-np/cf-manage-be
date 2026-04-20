package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "debt")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Debt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "debt_code", length = 255)
    private String debtCode;

    @Column(name = "debt_name", nullable = false, length = 255)
    private String debtName;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal totalMoney;

    // Không default
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaId;

    @Column(name = "paid_at")
    private Instant paIdAt;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_debt_supplier_id"))
    private Supplier supplier;
}
