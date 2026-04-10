package com.duyminhdev.cf_manager.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSchedulerDedupService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean acquire(String key, Duration ttl) {
        try {
            Boolean inserted = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(inserted);
        } catch (Exception ex) {
            log.warn("Dedup fallback for key={} because Redis operation failed: {}", key, ex.getMessage());
            return true;
        }
    }
}
