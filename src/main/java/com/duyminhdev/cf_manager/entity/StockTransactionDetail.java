package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transaction_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransactionDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transaction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_transaction_detail_stock_transaction_id"))
    private StockTransaction stockTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_level_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_transaction_detail_stock_level_id"))
    private StockLevel stockLevel;
}
