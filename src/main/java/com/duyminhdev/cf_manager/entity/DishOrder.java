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

    /** Tổng tiền gốc trước giảm giá = Σ(dish.price × quantity). Không thay đổi sau khi apply voucher. */
    @Column(name = "total_bill", precision = 18, scale = 2)
    private BigDecimal totalBill;

    /**
     * Số tiền được giảm từ voucher.
     * NULL nếu đơn không dùng voucher.
     */
    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    /**
     * Số tiền khách thực trả = totalBill - discountAmount.
     * Được lưu cứng để query nhanh (list-by-table, order-history hiển thị đúng).
     * NULL nếu đơn không dùng voucher (= totalBill).
     */
    @Column(name = "final_total", precision = 18, scale = 2)
    private BigDecimal finalTotal;

    /**
     * Voucher đã áp dụng cho đơn này.
     * NULL nếu đơn không dùng voucher.
     * Là nguồn gốc dữ liệu để rollback khi hủy đơn.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_dish_order_voucher_id"))
    private Voucher voucher;

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
