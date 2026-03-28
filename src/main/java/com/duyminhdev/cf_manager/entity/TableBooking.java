package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_booking")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TableBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "booking_at", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "check_in_at")
    private LocalDateTime checkinTime;

    @Column(name = "booking_status", nullable = false, length = 255)
    private String bookingStatus;

    @Column(name = "deposit_amount", precision = 18, scale = 0)
    private BigDecimal deposit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_booking_account_id"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_booking_dining_table_id"))
    private TableEntity table;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "note", length = 255)
    private String note;
}
