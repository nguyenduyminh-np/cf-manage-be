package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.Instant;

@Entity
@Table(name = "account_token")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_token_account_id"))
    private Account account;

    @Column(name = "refresh_token", unique = true, length = 255)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;

    @Column(name = "access_token_jti", unique = true, length = 255)
    private String accessTokenJti;

    @Builder.Default
    @Column(name = "is_revoked", nullable = false, columnDefinition = "tinyint(1) DEFAULT 0")
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private Instant createdTime;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Instant updatedTime;
}
