package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transaction_draft_detail", indexes = {
    @Index(name = "idx_stock_transaction_draft_detail_ingredient_id", columnList = "ingredient_id"),
    @Index(name = "idx_stock_transaction_draft_detail_stock_level_id", columnList = "stock_level_id"),
    @Index(name = "idx_stock_transaction_draft_detail_stock_transaction_id", columnList = "stock_transaction_id")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransactionDraftDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Trong DB ghi rành rành UnitPrice decimal(18, 2)
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "note", length = 500)
    private String note;

    @Builder.Default
    @Column(name = "create_new_batch", nullable = false, columnDefinition = "tinyint(1) DEFAULT 0")
    private Boolean createNewBatch = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    // VIRTUAL FK: Không có constraint khóa ngoại vật lý trong file SQL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transaction_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StockTransaction stockTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_level_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StockLevel stockLevel;
}
