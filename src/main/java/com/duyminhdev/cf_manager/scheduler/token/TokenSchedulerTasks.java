package com.duyminhdev.cf_manager.scheduler.token;

import com.duyminhdev.cf_manager.service.token.TokenLifecycleService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "token.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class TokenSchedulerTasks {

    private final TokenLifecycleService tokenLifecycleService;

    @Scheduled(cron = "${token.scheduler.cron:0 */5 * * * *}")
    @SchedulerLock(name = "tokenSchedulerRevokeExpiredRefreshTokens",
            lockAtMostFor = "${token.scheduler.lock-at-most:PT50S}",
            lockAtLeastFor = "${token.scheduler.lock-at-least:PT2S}")
    public void revokeExpiredRefreshTokens() {
        tokenLifecycleService.revokeExpiredRefreshTokens();
    }
}
