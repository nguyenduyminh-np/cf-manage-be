package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "account")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "account_code", length = 255)
    private String accountCode;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Lob
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Lob
    @Column(name = "photo")
    private String photo;

    @Column(name = "date_of_birth", nullable = false)
    private Instant dob;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    // FK_Account_RoleId_Role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_account_role_id"))
    private Role role;
}
