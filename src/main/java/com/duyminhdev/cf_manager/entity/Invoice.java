package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "invoice_code", length = 255)
    private String invoiceCode;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal totalMoney;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Builder.Default
    @Column(name = "guest_count", columnDefinition = "int DEFAULT 1")
    private Integer totalGuest = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_account_id"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_dining_table_id"))
    private TableEntity table;
}
