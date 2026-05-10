package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Junction table: track ai đã đọc notification nào.
 * UNIQUE (notification_id, account_id) — đảm bảo mỗi cặp chỉ lưu 1 lần.
 *
 * Schema migration cần chạy:
 *   CREATE TABLE IF NOT EXISTS notification_read (
 *     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     notification_id INT    NOT NULL,
 *     account_id      INT    NOT NULL,
 *     read_at         DATETIME NOT NULL DEFAULT NOW(),
 *     UNIQUE KEY uq_noti_read (notification_id, account_id),
 *     CONSTRAINT fk_nr_notification FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE CASCADE,
 *     CONSTRAINT fk_nr_account      FOREIGN KEY (account_id)      REFERENCES account(id)      ON DELETE CASCADE
 *   );
 */
@Entity
@Table(
    name = "notification_read",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_noti_read",
        columnNames = {"notification_id", "account_id"}
    )
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationRead {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_nr_notification"))
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_nr_account"))
    private Account account;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;
}
