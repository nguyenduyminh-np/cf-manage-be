package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_booking")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TableBooking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "expected_arrive_time", nullable = false)
    private LocalDateTime expectedArriveTime;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "expected_check_out")
    private LocalDateTime expectedCheckOut;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "booking_status", nullable = false, length = 255)
    private String bookingStatus;

    @Column(name = "deposit_amount", precision = 18, scale = 0)
    private BigDecimal depositAmount;

    @Builder.Default
    @Column(name = "deposit_paid", nullable = false, columnDefinition = "tinyint(1) DEFAULT 0")
    private Boolean depositPaid = false;

    @Column(name = "deposit_paid_at")
    private LocalDateTime depositPaidAt;

    @Builder.Default
    @Column(name = "is_deposit_forfeited", nullable = false, columnDefinition = "tinyint(1) DEFAULT 0")
    private Boolean depositForfeited = false;

    @Column(name = "deposit_txn_ref", length = 255)
    private String depositTxnRef;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "note", length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_booking_account_id"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_booking_dining_table_id"))
    private TableEntity table;
}
