package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Đại diện cho một mã voucher giảm giá.
 *
 * <p>Hai loại giảm giá:
 * <ul>
 *   <li>{@code PERCENT} – giảm theo %, bị giới hạn bởi {@code maxDiscount}</li>
 *   <li>{@code FIXED}   – giảm số tiền cố định</li>
 * </ul>
 *
 * <p>{@code @Version} được dùng để hỗ trợ optimistic lock ở tầng JPA,
 * phối hợp với native UPDATE atomic để chống race condition khi nhiều đơn
 * cùng apply voucher cuối.
 */
@Entity
@Table(name = "voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Mã voucher – unique, không phân biệt hoa/thường khi validate. */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    /** Mô tả nội dung / điều kiện áp dụng. */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Loại giảm giá: {@code "PERCENT"} hoặc {@code "FIXED"}.
     * DB lưu dạng ENUM('PERCENT','FIXED') – Java map qua String để linh hoạt.
     */
    @Column(name = "discount_type", columnDefinition = "ENUM('PERCENT','FIXED')", nullable = false)
    private String discountType;

    /**
     * Giá trị giảm:
     * <ul>
     *   <li>PERCENT: 5 → giảm 5%</li>
     *   <li>FIXED: 50000 → giảm 50,000đ</li>
     * </ul>
     */
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    /** Giá trị đơn hàng tối thiểu để áp dụng voucher. Null = không giới hạn. */
    @Column(name = "min_order_amount", precision = 18, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * Số tiền giảm tối đa khi loại là {@code PERCENT}.
     * Null = không giới hạn trần.
     * Ví dụ: discount 20% nhưng maxDiscount = 100,000đ → không giảm quá 100k.
     */
    @Column(name = "max_discount", precision = 18, scale = 2)
    private BigDecimal maxDiscount;

    /** Tổng số lượt được phép sử dụng. Null = không giới hạn. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** Số lượt đã sử dụng thực tế. Tăng bằng native UPDATE để đảm bảo atomic. */
    @Builder.Default
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    /** Ngày bắt đầu hiệu lực. Null = hiệu lực ngay từ khi tạo. */
    @Column(name = "start_date")
    private Instant startDate;

    /** Ngày hết hạn. Null = không hết hạn. */
    @Column(name = "end_date")
    private Instant endDate;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    /**
     * Tài khoản đã tạo voucher (admin/manager).
     * Mapping với cột {@code created_by} trong bảng {@code voucher}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true,
            foreignKey = @ForeignKey(name = "fk_voucher_created_by"))
    private Account createdBy;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    /**
     * Optimistic lock: phối hợp với native UPDATE để phát hiện
     * concurrent modification ở tầng JPA.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
