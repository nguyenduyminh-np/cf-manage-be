package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.response.notification.NotificationResponseDTO;
import com.duyminhdev.cf_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/v1/notification/my?page=0&size=20
     * Lấy danh sách notification của user hiện tại.
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<List<NotificationResponseDTO>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new ApiResponse<>(200, "SUCCESS", notificationService.getMyNotifications(page, size));
    }

    /**
     * POST /api/v1/notification/read
     * Body: { "id": 42 }
     * Đánh dấu một notification là đã đọc.
     */
    @PostMapping("/read")
    public ApiResponse<Void> markRead(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        notificationService.markRead(id);
        return new ApiResponse<>(200, "MARK_READ_SUCCESS");
    }

    /**
     * POST /api/v1/notification/read-all
     * Đánh dấu tất cả notification của user là đã đọc.
     */
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return new ApiResponse<>(200, "MARK_ALL_READ_SUCCESS");
    }
}
