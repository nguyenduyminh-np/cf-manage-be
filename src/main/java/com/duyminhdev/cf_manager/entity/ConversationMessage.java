package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Lưu lịch sử hội thoại của chatbot.
 * Mỗi tin nhắn (của user hoặc assistant) được lưu thành một bản ghi.
 */
@Entity
@Table(name = "conversation_messages", indexes = {
        @Index(name = "idx_conv_msg_user_session", columnList = "user_id, session_id"),
        @Index(name = "idx_conv_msg_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Username của nhân viên đã đăng nhập, hoặc "guest_<sessionId>" cho khách vãng lai.
     */
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    /**
     * ID phiên chat — UUID do client tạo hoặc server sinh.
     */
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    /**
     * "user" = tin nhắn của người dùng, "assistant" = phản hồi của chatbot.
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    /**
     * Nội dung tin nhắn.
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
