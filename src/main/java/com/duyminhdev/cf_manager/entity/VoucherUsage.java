package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lịch sử sử dụng voucher theo từng đơn hàng.
 *
 * <p>Mỗi bản ghi đại diện cho 1 lần voucher được áp dụng thành công
 * vào 1 {@link DishOrder}. Ràng buộc UNIQUE {@code (voucher_id, dish_order_id)}
 * đảm bảo không áp cùng voucher 2 lần cho 1 đơn.
 *
 * <p>Khi hủy đơn: xóa bản ghi này và giảm {@code voucher.usedCount}.
 * {@code usedCount} trong bảng {@code voucher} có thể tính lại từ bảng này.
 */
@Entity
@Table(
        name = "voucher_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_voucher_order",
                columnNames = {"voucher_id", "dish_order_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Snapshot số tiền thực tế được giảm cho đơn này. */
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount;

    /** Thời điểm áp dụng voucher. DB tự điền nếu NULL nhờ DEFAULT CURRENT_TIMESTAMP. */
    @Column(name = "used_at", nullable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "voucher_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_usage_voucher_id")
    )
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dish_order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_usage_dish_order_id")
    )
    private DishOrder dishOrder;

    /** Nhân viên thực hiện thao tác thanh toán (dùng để audit). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_usage_account_id")
    )
    private Account account;
}
