package com.duyminhdev.cf_manager.security;

import com.duyminhdev.cf_manager.dto.records.JwtProperties;
import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.security.jwt.RSAKeysUtil;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    private final String issuer;
    private final long accessTtlMinutes;
    private final long refreshTtlDays;
    private final RSAKeysUtil keyProvider;
    private final JwtParser accessTokenParser;

    private final Clock clock = Clock.systemUTC();
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(JwtProperties props, RSAKeysUtil keyProvider) {
        this.issuer = props.issuer();
        this.accessTtlMinutes = props.accessTokenTtlMinutes();
        this.refreshTtlDays = props.refreshTokenTtlDays();
        this.keyProvider = keyProvider;

        this.accessTokenParser = Jwts.parser()
                .verifyWith(keyProvider.publicKey())
                .requireIssuer(issuer)
                .build();
    }

    // ===================== Token Generation =====================

    public String generateAccessToken(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", account.getId());
        claims.put("role", account.getRole() != null ? account.getRole().getRoleCode() : "USER");
        claims.put("status", String.valueOf(account.getIsActive()));

        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(accessExpiresInSeconds());

        return Jwts.builder()
                .claims(claims)
                .subject(account.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString()) // JTI for instant revocation
                .signWith(keyProvider.privateKey())
                .compact();
    }

    /**
     * Opaque refresh token: Base64-encoded secure random bytes (NOT a JWT).
     */
    public String generateRefreshToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public long accessExpiresInSeconds() {
        return accessTtlMinutes * 60;
    }

    public Instant refreshExpiresAt() {
        return Instant.now(clock).plusSeconds(refreshTtlDays * 24L * 3600L);
    }

    // ===================== Claim Extractors =====================

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        Jws<Claims> jws = accessTokenParser.parseSignedClaims(token);
        return jws.getPayload();
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp.before(Date.from(Instant.now(clock)));
    }

    // ===================== Validation =====================

    public boolean validateToken(String token, CustomUserDetail userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
