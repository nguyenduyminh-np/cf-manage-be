package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.response.notification.NotificationResponseDTO;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * Lấy danh sách notification của user hiện tại (phân trang).
     *
     * @param page trang bắt đầu từ 0
     * @param size số phần tử mỗi trang (mặc định 20)
     */
    PageResponse<List<NotificationResponseDTO>> getMyNotifications(int page, int size);

    /**
     * Đánh dấu một notification cụ thể là đã đọc.
     * Chỉ cho phép với notification thuộc về user hiện tại.
     */
    void markRead(Integer notificationId);

    /**
     * Đánh dấu tất cả notification của user hiện tại là đã đọc.
     */
    void markAllRead();

    /**
     * Lưu một notification từ WebSocket event vào DB.
     * Được gọi bởi BookingNotificationServiceImpl sau khi gửi WS.
     * Chạy @Async — không block luồng WS.
     *
     * @param topic   WS topic, ví dụ "/topic/table-alerts"
     * @param envelope payload đã được normalizePayload()
     */
    void saveFromWsEvent(String topic, Map<String, Object> envelope);
}
