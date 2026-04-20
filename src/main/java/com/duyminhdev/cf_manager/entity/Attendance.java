package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "attendance")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "check_in_at", nullable = false)
    private Instant checkInTime;

    @Column(name = "check_out_at")
    private Instant checkOutTime;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @Column(name = "work_hours")
    private Integer workHours;

    // fk_Attendance_AccountId_Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_attendance_account_id"))
    private Account account;
}
