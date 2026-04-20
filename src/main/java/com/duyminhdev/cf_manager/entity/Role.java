package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "role")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "role_code", length = 255)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 255)
    private String roleName;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    // Trong DB là Active tinyint(1) DEFAULT NULL -> Không có default, nullable
    @Column(name = "is_active")
    private Boolean active;
}
