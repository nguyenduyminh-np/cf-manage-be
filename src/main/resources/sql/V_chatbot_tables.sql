-- ============================================================
-- Migration: Tạo bảng hỗ trợ module AI Chatbot
-- File: V_chatbot_tables.sql
-- Chạy thủ công hoặc tích hợp vào Flyway/Liquibase
-- ============================================================

-- Bảng lưu lịch sử hội thoại chatbot
CREATE TABLE IF NOT EXISTS `conversation_messages` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    VARCHAR(255) NOT NULL COMMENT 'Username nhân viên hoặc guest_<sessionId>',
    `session_id` VARCHAR(255) NOT NULL COMMENT 'UUID phiên chat',
    `role`       VARCHAR(20)  NOT NULL COMMENT 'user | assistant',
    `content`    TEXT         NOT NULL COMMENT 'Nội dung tin nhắn',
    `created_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_conv_msg_user_session` (`user_id`, `session_id`),
    INDEX `idx_conv_msg_created_at`   (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Lịch sử hội thoại AI Chatbot';

-- Bảng lưu sở thích người dùng chatbot
CREATE TABLE IF NOT EXISTS `user_preferences` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`               VARCHAR(255) NOT NULL COMMENT 'Username hoặc guest_<sessionId>',
    `preferred_dish_type`   VARCHAR(100)          COMMENT 'Loại đồ uống yêu thích',
    `preferred_table_floor` INT                   COMMENT 'Tầng hay ngồi',
    `preferred_price_range` VARCHAR(20)           COMMENT 'LOW | MEDIUM | HIGH',
    `total_bookings`        INT          NOT NULL DEFAULT 0 COMMENT 'Tổng lần đặt bàn qua chatbot',
    `last_booked_table_id`  BIGINT                COMMENT 'ID bàn đặt gần nhất',
    `notes`                 TEXT                  COMMENT 'Ghi chú tùy chỉnh',
    `updated_at`            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_user_pref_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Sở thích người dùng AI Chatbot';
