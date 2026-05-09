package com.duyminhdev.cf_manager.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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
     * Danh sách hành động gợi ý cho FE render thành quick-reply buttons.
     * VD: "Xem menu", "Đặt bàn ngay", "Xem doanh thu hôm nay".
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SuggestedAction> suggestedActions;

    /**
     * Dữ liệu có cấu trúc (bảng, list, KPI...) để FE render UI đặc biệt thay vì plain text.
     * Có thể là List, Map, hoặc DTO bất kỳ — FE tự phân biệt dựa trên intentType.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object structuredData;

    /**
     * Constructor nhanh — chỉ cần reply và sessionId.
     */
    public ChatResponse(String reply, String sessionId) {
        this.reply = reply;
        this.sessionId = sessionId;
        this.timestamp = Instant.now();
    }

    /**
     * Hành động gợi ý kèm theo response để FE hiển thị dưới dạng nút bấm.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuggestedAction {

        /** Nhãn hiển thị trên nút (VD: "Xem menu") */
        private String label;

        /**
         * Hành động khi bấm nút. Format:
         * - "CHAT:..." → gửi tin nhắn chat mới (VD: "CHAT:đặt bàn 4 người tối nay")
         * - "NAVIGATE:..." → chuyển trang (VD: "NAVIGATE:/menu")
         */
        private String action;
    }
}
