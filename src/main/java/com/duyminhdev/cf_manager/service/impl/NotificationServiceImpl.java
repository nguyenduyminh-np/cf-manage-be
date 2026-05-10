package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.response.notification.NotificationResponseDTO;
import com.duyminhdev.cf_manager.entity.Notification;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.AccountRepository;
import com.duyminhdev.cf_manager.repository.NotificationReadRepository;
import com.duyminhdev.cf_manager.repository.NotificationRepository;
import com.duyminhdev.cf_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository     notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final AccountRepository          accountRepository;

    // ─────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<List<NotificationResponseDTO>> getMyNotifications(int page, int size) {
        Integer accountId = currentAccountId();
        Page<Notification> pageResult = notificationRepository
                .findAllActive(PageRequest.of(page, size));

        List<Integer> notifIds = pageResult.getContent().stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        // 1 query để lấy danh sách đã đọc — không N+1
        Set<Integer> readIds = notifIds.isEmpty()
                ? Collections.emptySet()
                : notificationReadRepository.findReadIdsByAccountIdAndNotifIds(accountId, notifIds);

        List<NotificationResponseDTO> rows = pageResult.getContent().stream()
                .map(n -> toDto(n, readIds.contains(n.getId())))
                .collect(Collectors.toList());

        PageResponse<List<NotificationResponseDTO>> response = new PageResponse<>();
        response.setRows(rows);
        response.setPageNo(page);
        response.setPageSize(size);
        response.setTotalElements((int) pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        return response;
    }

    @Override
    @Transactional
    public void markRead(Integer notificationId) {
        Integer accountId = currentAccountId();
        if (!notificationRepository.existsById(notificationId)) {
            throw new InvalidDataException("Không tìm thấy thông báo.");
        }
        // INSERT IGNORE — idempotent, không lỗi nếu đã đọc
        notificationReadRepository.markReadById(notificationId, accountId);
    }

    @Override
    @Transactional
    public void markAllRead() {
        Integer accountId = currentAccountId();
        // INSERT IGNORE ... SELECT — 1 SQL, không loop
        notificationReadRepository.markAllReadByAccountId(accountId);
    }

    /**
     * Shared Notification — chỉ 1 INSERT duy nhất cho toàn bộ hệ thống.
     * Không loop theo số nhân viên.
     */
    @Override
    @Async
    public void saveFromWsEvent(String topic, Map<String, Object> envelope) {
        if (!isAlertTopic(topic)) {
            return;
        }

        try {
            String eventType  = String.valueOf(envelope.getOrDefault("event", "UNKNOWN"));
            String message    = String.valueOf(envelope.getOrDefault("message", ""));
            Object atObj      = envelope.get("at");
            Instant createdAt = atObj instanceof Instant i ? i : Instant.now();

            Notification notification = Notification.builder()
                    .name(eventType)
                    .description(message)
                    .url(topic)
                    .createdTime(createdAt)
                    .active(true)
                    .build();

            notificationRepository.save(notification);

            log.debug("[Noti] Shared notification saved: event={}, topic={}", eventType, topic);
        } catch (Exception ex) {
            log.warn("[Noti] Persist failed: topic={}, error={}", topic, ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────

    private boolean isAlertTopic(String topic) {
        return "/topic/table-alerts".equals(topic)
                || "/topic/booking-updates".equals(topic)
                || "/topic/deposit-events".equals(topic)
                || "/topic/kitchen-orders".equals(topic);
    }

    private NotificationResponseDTO toDto(Notification n, boolean read) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .title(n.getName())
                .description(n.getDescription())
                .url(n.getUrl())
                .createdAt(n.getCreatedTime())
                .read(read)
                .eventType(n.getName())
                .topic(n.getUrl())
                .build();
    }

    private Integer currentAccountId() {
        String username = currentUsername();
        return accountRepository.findByUsername(username)
                .map(a -> a.getId())
                .orElseThrow(() -> new InvalidDataException("Account không tồn tại."));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidDataException("Chưa xác thực.");
        }
        return auth.getName();
    }
}
