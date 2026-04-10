package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.constant.BookingSchedulerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingNotificationServiceImpl implements BookingNotificationService {

    private final BookingSchedulerDedupService bookingSchedulerDedupService;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    @Override
    public void sendOnce(String topic, String dedupKey, Map<String, Object> payload) {
        if (!bookingSchedulerDedupService.acquire(dedupKey, BookingSchedulerConstant.NOTIFICATION_DEDUP_TTL)) {
            return;
        }

        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null) {
            log.debug("SimpMessagingTemplate is not configured, skipping notification. topic={}, payload={}", topic, payload);
            return;
        }

        messagingTemplate.convertAndSend(topic, payload);
    }
}
