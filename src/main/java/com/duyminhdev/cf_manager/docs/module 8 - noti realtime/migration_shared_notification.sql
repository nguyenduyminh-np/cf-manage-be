-- ============================================================
-- Migration: Fan-out Write → Shared Notification
-- Chạy script này trước khi restart Spring Boot
-- ============================================================

-- 1. Làm nullable các FK cũ (không xóa column để tránh mất dữ liệu cũ)
ALTER TABLE notification
    MODIFY COLUMN account_id      INT NULL,
    MODIFY COLUMN sender_role_id  INT NULL,
    MODIFY COLUMN notification_status_id INT NULL;

-- 2. Tạo bảng notification_read
CREATE TABLE IF NOT EXISTS notification_read (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    notification_id INT          NOT NULL,
    account_id      INT          NOT NULL,
    read_at         DATETIME     NOT NULL DEFAULT NOW(),
    UNIQUE KEY uq_noti_read (notification_id, account_id),
    CONSTRAINT fk_nr_notification FOREIGN KEY (notification_id)
        REFERENCES notification(id) ON DELETE CASCADE,
    CONSTRAINT fk_nr_account FOREIGN KEY (account_id)
        REFERENCES account(id) ON DELETE CASCADE
);

-- 3. (Tuỳ chọn) Xóa dữ liệu cũ theo mô hình Fan-out Write
--    Bỏ comment nếu muốn làm sạch DB:
-- TRUNCATE TABLE notification;
