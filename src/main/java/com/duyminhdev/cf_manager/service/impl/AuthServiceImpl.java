package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.AuthResponse;
import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.AccountToken;
import com.duyminhdev.cf_manager.entity.Role;
import com.duyminhdev.cf_manager.exceptions.DuplicatedUsernameException;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.AccountRepository;
import com.duyminhdev.cf_manager.repository.AccountTokenRepository;
import com.duyminhdev.cf_manager.repository.RoleRepository;
import com.duyminhdev.cf_manager.security.JwtService;
import com.duyminhdev.cf_manager.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepo;
    private final AccountTokenRepository tokenRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            AccountRepository accountRepo,
            AccountTokenRepository tokenRepo,
            RoleRepository roleRepo,
            JwtService jwtService,
            @org.springframework.context.annotation.Lazy AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepo = accountRepo;
        this.tokenRepo = tokenRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponse login(String username, String rawPassword) {
        // Authenticate via Spring's DaoAuthenticationProvider
        // Must catch AuthenticationException here; otherwise ExceptionTranslationFilter
        // intercepts it at filter level → calls AuthenticationEntryPoint → returns 401
        // BEFORE @RestControllerAdvice can handle it.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword)
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidDataException("Invalid username or password");
        } catch (DisabledException ex) {
            throw new InvalidDataException("User account is disabled");
        }

        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (account.getActive() == null || !account.getActive()) {
            throw new DisabledException("User is not active");
        }

        return issueTokens(account, "LOGIN_SUCCESSFULLY");
    }

    @Override
    @Transactional
    public AuthResponse register(String username, String rawPassword, String fullName) {
        if (accountRepo.existsByUsername(username)) {
            throw new DuplicatedUsernameException(username);
        }

        Account account = buildAccount(username, rawPassword, fullName, "PC-008");
        account = accountRepo.save(account);

        return issueTokens(account, "REGISTER_SUCCESSFULLY");
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        AccountToken token = tokenRepo.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        Account account = token.getAccount();

        if (account.getActive() == null || !account.getActive()) {
            throw new DisabledException("User is not active");
        }

        // Enforce refresh token TTL
        LocalDateTime expAt = token.getRefreshTokenExpiresAt();
        if (expAt == null || expAt.atZone(ZoneId.systemDefault()).toInstant().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        // Revoke old token
        token.setRevoked(true);
        tokenRepo.save(token);

        return issueTokens(account, "REFRESH_TOKEN_SUCCESSFULLY");
    }

    @Override
    @Transactional
    public AuthResponse logout(String refreshToken) {
        AccountToken token = tokenRepo.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // Revoke the token — this invalidates both refresh token and access token (JTI mismatch)
        token.setRevoked(true);
        tokenRepo.save(token);

        return AuthResponse.builder()
                .message("LOGOUT_SUCCESSFULLY")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(String username, String rawPassword, String fullName) {
        if (accountRepo.existsByUsername(username)) {
            throw new DuplicatedUsernameException(username);
        }

        Account account = buildAccount(username, rawPassword, fullName, "ADMIN");
        account = accountRepo.save(account);

        return issueTokens(account, "REGISTER_SUCCESSFULLY");
    }

    // ===================== Helper Methods =====================

    /**
     * Issues new access + refresh tokens, revokes any existing active token, and saves a new AccountToken.
     */
    private AuthResponse issueTokens(Account account, String message) {
        // Revoke any existing active token for this account
        tokenRepo.findByAccountAndRevokedFalse(account).ifPresent(oldToken -> {
            oldToken.setRevoked(true);
            tokenRepo.save(oldToken);
        });

        // Generate new tokens
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken();
        String jti = jwtService.extractJti(accessToken);
        Instant refreshExpiresAt = jwtService.refreshExpiresAt();

        // Save new AccountToken
        AccountToken newToken = AccountToken.builder()
                .account(account)
                .refreshToken(refreshToken)
                .refreshTokenExpiresAt(LocalDateTime.ofInstant(refreshExpiresAt, ZoneId.systemDefault()))
                .accessTokenJti(jti)
                .revoked(false)
                .build();
        tokenRepo.save(newToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.accessExpiresInSeconds())
                .message(message)
                .build();
    }

    private Account buildAccount(String username, String rawPassword, String fullName, String roleCode) {
        // Look up by RoleCode (e.g. "ADMIN") matching actual DB values
        Role role = roleRepo.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found with code: " + roleCode));

        return Account.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .photo("images/userdefault.jpg")
                .active(true)
                .createdTime(LocalDateTime.now())
                .dob(LocalDateTime.of(2000, 1, 1, 0, 0))
                .role(role)
                .build();
    }
}
