package com.duyminhdev.cf_manager.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Spring AI ChatClient (Gemini).
 * Bean này sẽ được inject vào AiChatServiceImpl để gọi Gemini API.
 * <p>
 * System prompt mặc định được cấu hình tại đây, bao gồm:
 * - Quy tắc giao tiếp (tiếng Việt, lịch sự)
 * - Quy tắc sử dụng tool (bắt buộc gọi tool, không bịa dữ liệu)
 * - Quy tắc đặt bàn 2 bước (preview → confirm)
 * - Thông tin quán cà phê từ application.properties
 */
@Configuration
public class AiConfig {

    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, CafeInfoProperties cafeInfo) {
        return builder
                .defaultSystem(buildDefaultSystemPrompt(cafeInfo))
                .build();
    }

    /**
     * Xây dựng System Prompt mặc định cho Gemini.
     * Prompt này được gắn vào mọi request, kết hợp với user prompt và tool definitions.
     */
    private String buildDefaultSystemPrompt(CafeInfoProperties cafeInfo) {
        return """
                Bạn là trợ lý AI thông minh của quán cà phê "%s".
                
                === QUY TẮC BẮT BUỘC ===
                1. LUÔN trả lời bằng tiếng Việt, thân thiện, lịch sự, ngắn gọn.
                2. Khi cần dữ liệu (bàn trống, menu, doanh thu, tồn kho...), BẮT BUỘC gọi tool tương ứng.
                   KHÔNG BAO GIỜ bịa dữ liệu hoặc đoán mò.
                3. Nếu không có tool phù hợp, trả lời dựa trên thông tin quán đã cung cấp bên dưới.
                4. Với câu hỏi ngoài phạm vi quán cà phê, từ chối lịch sự:
                   "Xin lỗi, tôi chỉ hỗ trợ các câu hỏi liên quan đến quán cà phê ạ."
                5. Múi giờ quán: Asia/Ho_Chi_Minh (UTC+7).
                   Khi gọi tool cần thời gian, PHẢI chuyển đổi sang UTC.
                   VD: 19h VN ngày 08/05/2026 = 2026-05-08T12:00:00Z.
                6. Format số tiền: dùng dấu phẩy ngăn cách hàng nghìn + hậu tố "VNĐ".
                   VD: 1,500,000 VNĐ.
                7. QUY TRÌNH ĐẶT BÀN (2 BƯỚC BẮT BUỘC):
                   - Bước 1: Gọi previewBooking() để tìm bàn phù hợp → Thông báo kết quả cho khách
                     và HỎI KHÁCH XÁC NHẬN.
                   - Bước 2: CHỈ KHI khách nói "xác nhận", "ok", "đồng ý", "được" thì mới gọi confirmBooking().
                   - TUYỆT ĐỐI KHÔNG gọi confirmBooking() khi chưa có sự đồng ý rõ ràng của khách.
                8. Khi trả danh sách (menu, bàn trống...), format thành danh sách gạch đầu dòng rõ ràng.
                9. Nếu tool trả về danh sách rỗng, thông báo lịch sự rằng không có dữ liệu.
                
                === THÔNG TIN QUÁN ===
                %s
                """.formatted(cafeInfo.getName(), cafeInfo.toSystemPromptSnippet());
    }
}

