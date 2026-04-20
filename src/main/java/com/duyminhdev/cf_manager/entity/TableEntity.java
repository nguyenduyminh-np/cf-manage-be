package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dining_table") // Đổi sang dining_table để khớp schema
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "table_code", length = 255)
    private String tableCode;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "slot")
    private Integer slot;

    @Column(name = "table_name", nullable = false, length = 255)
    private String tableName;

    @Column(name = "table_status", nullable = false, length = 255)
    private String tableStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    // Không default
    @Column(name = "is_active", nullable = false)
    private Boolean active;
}
