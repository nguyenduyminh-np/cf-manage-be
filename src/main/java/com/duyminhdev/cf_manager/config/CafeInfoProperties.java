package com.duyminhdev.cf_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình thông tin quán cà phê dùng trong System Prompt của AI Chatbot.
 * Toàn bộ thông tin được đọc từ application.properties với prefix "cafe.info".
 * Để tùy chỉnh thông tin quán, chỉnh sửa trực tiếp trong application.properties.
 */
@Component
@ConfigurationProperties(prefix = "cafe.info")
@Getter
@Setter
public class CafeInfoProperties {

    /** Tên quán */
    private String name = "CF Manager Coffee";

    /** Địa chỉ quán */
    private String address = "123 Nguyễn Văn Linh, Quận 7, TP. Hồ Chí Minh";

    /** Số điện thoại liên hệ */
    private String phone = "028.3456.7890";

    /** Giờ mở cửa (VD: "7:00") */
    private String openTime = "7:00";

    /** Giờ đóng cửa (VD: "22:00") */
    private String closeTime = "22:00";

    /** Có wifi hay không */
    private boolean wifi = true;

    /** Mật khẩu wifi (nếu có) */
    private String wifiPassword;

    /** Có bãi đỗ xe hay không */
    private boolean parking = true;

    /** Mô tả ngắn về quán */
    private String description = "Quán cà phê hiện đại, không gian thoáng đãng.";

    /** Số tầng của quán */
    private int floors = 3;

    /** Ghi chú chính sách đặc biệt */
    private String note;

    /**
     * Tạo đoạn text mô tả quán để đưa vào System Prompt.
     * Khi thêm thông tin mới, chỉ cần bổ sung property vào đây.
     */
    public String toSystemPromptSnippet() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== THÔNG TIN QUÁN ===\n");
        sb.append("Tên quán: ").append(name).append("\n");
        sb.append("Địa chỉ: ").append(address).append("\n");
        sb.append("Điện thoại: ").append(phone).append("\n");
        sb.append("Giờ mở cửa: ").append(openTime).append(" đến ").append(closeTime).append("\n");
        sb.append("Số tầng: ").append(floors).append(" tầng\n");
        if (wifi) {
            sb.append("Wifi: Có");
            if (wifiPassword != null && !wifiPassword.isBlank()) {
                sb.append(" — Mật khẩu: ").append(wifiPassword);
            }
            sb.append("\n");
        } else {
            sb.append("Wifi: Không\n");
        }
        sb.append("Bãi đỗ xe: ").append(parking ? "Có" : "Không").append("\n");
        if (description != null && !description.isBlank()) {
            sb.append("Mô tả: ").append(description).append("\n");
        }
        if (note != null && !note.isBlank()) {
            sb.append("Lưu ý: ").append(note).append("\n");
        }
        return sb.toString();
    }
}
