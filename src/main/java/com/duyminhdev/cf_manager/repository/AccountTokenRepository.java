package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.AccountToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AccountTokenRepository extends JpaRepository<AccountToken, Integer> {

    Optional<AccountToken> findByRefreshTokenAndRevokedFalse(String refreshToken);

    Optional<AccountToken> findByAccountAndRevokedFalse(Account account);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        update AccountToken t
                        set t.revoked = true
                        where t.revoked = false
                            and t.refreshTokenExpiresAt is not null
                            and t.refreshTokenExpiresAt < :now
                        """)
        int revokeExpiredRefreshTokens(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE AccountToken t SET t.revoked = true WHERE t.account.id = :accountId AND t.revoked = false")
    void revokeAllByAccountId(@Param("accountId") Integer accountId);
}
