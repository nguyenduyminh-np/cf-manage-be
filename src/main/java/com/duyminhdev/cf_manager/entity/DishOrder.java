package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dish_order")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DishOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @Column(name = "total_bill", precision = 18, scale = 2)
    private BigDecimal totalBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_order_status_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_order_dish_order_status_id"))
    private DishOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_order_dining_table_id"))
    private TableEntity table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dish_order_account_id"))
    private Account account;
}
