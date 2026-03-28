package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTokenRepository extends JpaRepository<AccountToken, Integer> {

    Optional<AccountToken> findByRefreshTokenAndIsRevokedFalse(String refreshToken);

    Optional<AccountToken> findByAccountAndIsRevokedFalse(Account account);
}
