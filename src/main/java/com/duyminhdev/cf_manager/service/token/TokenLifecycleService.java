package com.duyminhdev.cf_manager.service.token;

import com.duyminhdev.cf_manager.repository.AccountTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenLifecycleService {

    private final AccountTokenRepository accountTokenRepository;

    @Transactional
    public int revokeExpiredRefreshTokens() {
        int revokedCount = accountTokenRepository.revokeExpiredRefreshTokens(Instant.now());
        if (revokedCount > 0) {
            log.info("Revoked {} expired refresh token(s)", revokedCount);
        }
        return revokedCount;
    }
}
