package com.duyminhdev.cf_manager.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request gửi lên từ client khi nhắn tin với chatbot.
 */
@Getter
@Setter
public class ChatRequest {

    /**
     * Nội dung tin nhắn của người dùng.
     */
    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(max = 1000, message = "Tin nhắn không được vượt quá 1000 ký tự")
    private String message;

    /**
     * UUID phiên chat — client sinh và giữ nguyên suốt cuộc hội thoại.
     * Nếu null, server sẽ sinh mới.
     */
    private String sessionId;

    /**
     * Dùng cho endpoint /public: client có thể truyền userId tạm (VD: deviceId).
     * Với endpoint /staff, userId sẽ lấy từ JWT, field này bị bỏ qua.
     */
    private String userId;
}
