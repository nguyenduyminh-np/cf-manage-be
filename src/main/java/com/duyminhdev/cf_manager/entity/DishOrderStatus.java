package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dish_order_status")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DishOrderStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dish_order_status_code", length = 255)
    private String dishOrderStatusCode;

    @Column(name = "dish_order_status_name", nullable = false, length = 255)
    private String dishOrderStatusName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean isActive = true;
}
