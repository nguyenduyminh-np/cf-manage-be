package com.duyminhdev.cf_manager.security;

import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.AccountToken;
import com.duyminhdev.cf_manager.repository.AccountRepository;
import com.duyminhdev.cf_manager.repository.AccountTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepo;
    private final AccountTokenRepository tokenRepo;

    public CustomUserDetailsService(AccountRepository accountRepo, AccountTokenRepository tokenRepo) {
        this.accountRepo = accountRepo;
        this.tokenRepo = tokenRepo;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (account.getPassword() == null || account.getPassword().isBlank()) {
            throw new UsernameNotFoundException("User has no password set");
        }

        if (account.getActive() == null || !account.getActive()) {
            throw new UsernameNotFoundException("User inactive");
        }

        if (account.getRole() == null || account.getRole().getRoleCode() == null || account.getRole().getRoleCode().isBlank()) {
            throw new UsernameNotFoundException("User role is missing");
        }

        if (!Boolean.TRUE.equals(account.getRole().getActive())) {
            throw new UsernameNotFoundException("User role is inactive");
        }

        // Load the active (non-revoked) token's JTI for JTI comparison in the filter
        String activeJti = tokenRepo.findByAccountAndRevokedFalse(account)
                .map(AccountToken::getAccessTokenJti)
                .orElse(null);

        return new CustomUserDetail(account, activeJti);
    }
}
