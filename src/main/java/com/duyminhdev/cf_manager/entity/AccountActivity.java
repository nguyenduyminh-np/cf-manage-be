package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "account_activity")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "activity_code", length = 255)
    private String activityCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @Column(name = "activity_description", nullable = false, length = 500)
    private String activityDescription;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    // fk_AccountActivity_AccountId_Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_account_activity_account_id"))
    private Account account;
}
