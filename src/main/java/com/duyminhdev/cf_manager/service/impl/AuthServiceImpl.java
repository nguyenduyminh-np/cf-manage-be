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
import java.time.LocalDate;
import java.time.ZoneOffset;

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
    /**
     * Đăng nhập bằng username/password và phát hành cặp token mới.
     */
    public AuthResponse login(String username, String rawPassword) {
        /**
         * Flow login:
         * 1. Authenticate với Spring Security
         * 2. Load account theo username
         * 3. Validate account đang active
         * 4. Phát hành access/refresh token mới
         */
        // Authenticate via Spring's DaoAuthenticationProvider
        // Must catch AuthenticationException here; otherwise ExceptionTranslationFilter
        // intercepts it at filter level → calls AuthenticationEntryPoint → returns 401
        // BEFORE @RestControllerAdvice can handle it.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword)
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidDataException("Tên đăng nhập hoặc mật khẩu không chính xác");
        } catch (DisabledException ex) {
            throw new InvalidDataException("Tài khoản đã bị vô hiệu hóa");
        }

        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        if (account.getActive() == null || !account.getActive()) {
            throw new DisabledException("Tài khoản chưa được kích hoạt");
        }

        return issueTokens(account, "LOGIN_SUCCESSFULLY");
    }

    @Override
    @Transactional
    /**
     * Đăng ký tài khoản người dùng thường.
     */
    public AuthResponse register(String username, String rawPassword, String fullName) {
        /**
         * Flow register user:
         * 1. Kiểm tra trùng username
         * 2. Build account với role mặc định
         * 3. Save account mới
         * 4. Phát hành token đăng nhập ngay
         */
        if (accountRepo.existsByUsername(username)) {
            throw new DuplicatedUsernameException(username);
        }

        Account account = buildAccount(username, rawPassword, fullName, "PC-008");
        account = accountRepo.save(account);

        return issueTokens(account, "REGISTER_SUCCESSFULLY");
    }

    @Override
    @Transactional
    /**
     * Làm mới access token bằng refresh token hợp lệ.
     */
    public AuthResponse refresh(String refreshToken) {
        /**
         * Flow refresh token:
         * 1. Tìm refresh token còn hiệu lực
         * 2. Validate account đang active
         * 3. Validate TTL của refresh token
         * 4. Revoke token cũ
         * 5. Phát hành token mới
         */
        AccountToken token = tokenRepo.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token không hợp lệ hoặc đã bị thu hồi"));

        Account account = token.getAccount();

        if (account.getActive() == null || !account.getActive()) {
            throw new DisabledException("Tài khoản chưa được kích hoạt");
        }

        // Enforce refresh token TTL
        Instant now = Instant.now();
        Instant expAt = token.getRefreshTokenExpiresAt();
        if (expAt == null || !expAt.isAfter(now)) {
            token.setRevoked(true);
            tokenRepo.save(token);
            throw new BadCredentialsException("Refresh token đã hết hiệu lực");
        }

        // Revoke old token
        token.setRevoked(true);
        tokenRepo.save(token);

        return issueTokens(account, "REFRESH_TOKEN_SUCCESSFULLY");
    }

    @Override
    @Transactional
    /**
     * Đăng xuất bằng cách thu hồi refresh token hiện tại.
     */
    public AuthResponse logout(String refreshToken) {
        /**
         * Flow logout:
         * 1. Tìm refresh token còn hiệu lực
         * 2. Đánh dấu token revoked
         * 3. Trả kết quả đăng xuất thành công
         */
        AccountToken token = tokenRepo.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token không hợp lệ hoặc đã bị thu hồi"));

        // Revoke the token — this invalidates both refresh token and access token (JTI mismatch)
        token.setRevoked(true);
        tokenRepo.save(token);

        return AuthResponse.builder()
                .message("LOGOUT_SUCCESSFULLY")
                .build();
    }

    @Override
    @Transactional
    /**
     * Đăng ký tài khoản quản trị viên.
     */
    public AuthResponse registerAdmin(String username, String rawPassword, String fullName) {
        /**
         * Flow register admin:
         * 1. Kiểm tra trùng username
         * 2. Build account với role ADMIN
         * 3. Save account mới
         * 4. Phát hành token đăng nhập ngay
         */
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
        // Bulk-revoke TẤT CẢ token active của account này trước khi phát token mới.
        // Dùng @Query JPQL thay vì find-then-save để tránh NonUniqueResultException
        // khi DB có nhiều row is_revoked=false (race condition / dữ liệu bẩn).
        tokenRepo.revokeAllByAccountId(account.getId());

        // Generate new tokens
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken();
        String jti = jwtService.extractJti(accessToken);
        Instant refreshExpiresAt = jwtService.refreshExpiresAt();

        // Save new AccountToken
        AccountToken newToken = AccountToken.builder()
                .account(account)
                .refreshToken(refreshToken)
            .refreshTokenExpiresAt(refreshExpiresAt)
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
                .userInfo(AuthResponse.UserInfo.builder()
                        .fullName(account.getFullName())
                        .phoneNumber(account.getPhoneNumber())
                        .build())
                .build();
    }

    private Account buildAccount(String username, String rawPassword, String fullName, String roleCode) {
        // Look up by RoleCode (e.g. "ADMIN") matching actual DB values
        Role role = roleRepo.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với mã: " + roleCode));

        return Account.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .photo("images/userdefault.jpg")
                .active(true)
            .createdTime(Instant.now())
            .dob(LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .role(role)
                .build();
    }
}
