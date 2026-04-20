package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_flow")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CashFlow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal totalMoney;

    @Column(name = "flow_type", nullable = false, length = 255)
    private String flowType;

    @Column(name = "note", nullable = false, length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cash_flow_account_id"))
    private Account account;
}
