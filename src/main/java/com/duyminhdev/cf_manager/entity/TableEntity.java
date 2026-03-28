package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dining_table")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "table_code", length = 255)
    private String tableCode;

    @Builder.Default
    @Column(name = "floor", columnDefinition = "int DEFAULT 1")
    private Integer floor = 1;

    @Column(name = "table_name", nullable = false, length = 255)
    private String tableName;

    @Column(name = "table_status", nullable = false, length = 255)
    private String tableStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
