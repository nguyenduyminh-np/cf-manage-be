package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "stock_transaction_code", length = 255)
    private String stockTransactionCode;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "transaction_type", nullable = false, length = 100)
    private String transactionType;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal totalMoney;

    @Column(name = "status", nullable = false, length = 100)
    private String status;

    @Column(name = "transaction_at")
    private Instant transactionDate;
    
    // Approval Tracking
    @Column(name = "approved_at")
    private Instant approvedDate;
    
    @Column(name = "approved_by_account_id") // Lưu ý: Database không set FK cho các cột UserBy này, nên dùng Integer
    private Integer approvedBy;
    
    @Column(name = "completed_at")
    private Instant completedDate;
    
    @Column(name = "completed_by_account_id")
    private Integer completedBy;
    
    @Column(name = "canceled_at")
    private Instant canceledDate;
    
    @Column(name = "canceled_by_account_id")
    private Integer canceledBy;

    @Lob
    @Column(name = "status_history")
    private String statusHistory;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_transaction_warehouse_id"))
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_transaction_account_id"))
    private Account account;
}
