package com.duyminhdev.cf_manager.service.booking;

import java.util.Map;

public interface BookingNotificationService {

    void sendOnce(String topic, String dedupKey, Map<String, Object> payload);
}
