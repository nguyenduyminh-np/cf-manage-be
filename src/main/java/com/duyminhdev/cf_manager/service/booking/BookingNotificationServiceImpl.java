package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import com.duyminhdev.cf_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingNotificationServiceImpl implements BookingNotificationService {

    private final BookingSchedulerDedupService bookingSchedulerDedupService;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;
    /** ObjectProvider để tránh circular dependency khi NotificationService cũng dùng Spring context */
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    @Override
    public void sendOnce(String topic, String dedupKey, Map<String, Object> payload) {
        if (!bookingSchedulerDedupService.acquire(dedupKey, BookingSchedulerConstant.NOTIFICATION_DEDUP_TTL)) {
            return;
        }

        Map<String, Object> envelope = normalizePayload(topic, dedupKey, payload);

        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null) {
            log.debug("SimpMessagingTemplate is not configured, skipping notification. topic={}, payload={}", topic, envelope);
            return;
        }

        messagingTemplate.convertAndSend(topic, envelope);

        // Persist bất đồng bộ vào DB — không block thread WS
        NotificationService notificationService = notificationServiceProvider.getIfAvailable();
        if (notificationService != null) {
            notificationService.saveFromWsEvent(topic, envelope);
        }
    }

    private Map<String, Object> normalizePayload(String topic, String dedupKey, Map<String, Object> payload) {
        Map<String, Object> safePayload = payload != null ? payload : Map.of();
        Map<String, Object> envelope = new LinkedHashMap<>(safePayload);

        Object canonicalEvent = envelope.get("event");
        if (canonicalEvent == null && envelope.containsKey("eventType")) {
            canonicalEvent = envelope.get("eventType");
        }
        envelope.put("event", canonicalEvent);

        Object at = envelope.get("at");
        if (at == null && envelope.containsKey("occurredAt")) {
            at = envelope.get("occurredAt");
        }
        if (at == null) {
            at = Instant.now();
        }
        envelope.put("at", at);

        envelope.put("topic", topic);
        envelope.put("dedupKey", dedupKey);
        envelope.put("eventId", UUID.randomUUID().toString());

        return envelope;
    }
}
