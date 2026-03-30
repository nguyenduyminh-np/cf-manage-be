package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_target")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialTarget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "target_revenue", nullable = false, precision = 18, scale = 0)
    private BigDecimal targetRevenue;

    @Column(name = "target_profit", precision = 18, scale = 0)
    private BigDecimal targetProfit;

    @Column(name = "period", length = 50)
    private String period;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;
}
