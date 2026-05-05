package com.duyminhdev.cf_manager.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response trả về từ chatbot sau mỗi lần xử lý tin nhắn.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    /**
     * Câu trả lời của chatbot.
     */
    private String reply;

    /**
     * ID phiên chat — trả về để client dùng cho lần nhắn kế tiếp.
     */
    private String sessionId;

    /**
     * Thời điểm tạo phản hồi (UTC).
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Loại intent đã được detect (debug / FE có thể dùng để hiển thị).
     * Nullable — chỉ trả khi cần.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String intentType;

    /**
     * Constructor nhanh — chỉ cần reply và sessionId.
     */
    public ChatResponse(String reply, String sessionId) {
        this.reply = reply;
        this.sessionId = sessionId;
        this.timestamp = Instant.now();
    }
}
