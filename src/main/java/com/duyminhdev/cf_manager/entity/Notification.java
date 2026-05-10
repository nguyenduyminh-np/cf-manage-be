package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Shared Notification — mỗi WS event chỉ tạo 1 bản ghi duy nhất.
 * Trạng thái đọc của từng nhân viên được lưu trong bảng NotificationRead.
 *
 * Schema migration cần chạy:
 *   ALTER TABLE notification MODIFY COLUMN account_id INT NULL;
 *   ALTER TABLE notification MODIFY COLUMN sender_role_id INT NULL;
 */
@Entity
@Table(name = "notification")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** WS event name, vd: "BOOKING_WALK_IN_CREATED", "TABLE_OCCUPIED_CONFLICT" */
    @Column(name = "name", length = 255)
    private String name;

    /** Human-readable message từ BE */
    @Lob
    @Column(name = "description")
    private String description;

    /** WS topic gốc, vd: "/topic/booking-updates", "/topic/table-alerts" */
    @Lob
    @Column(name = "url")
    private String url;

    @Column(name = "created_at")
    private Instant createdTime;

    @Column(name = "is_active")
    private Boolean active;

    /**
     * senderRole nullable — scheduler events không có người dùng cụ thể.
     * account_id đã bị loại bỏ khỏi mô hình Shared Notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_role_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_notification_sender_role_id"))
    private Role senderRole;
}
