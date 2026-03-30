package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 0)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdTime;

    // Bảng này trong DB KHÔNG có DEFAULT 1
    @Column(name = "is_active", nullable = false)
    private Boolean active;

    // VIRTUAL FK: SQL không hề có ALTER TABLE ... ADD CONSTRAINT cho InvoiceDetail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Dish dish;
}
