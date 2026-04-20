package com.duyminhdev.cf_manager.security.jwt;

import com.duyminhdev.cf_manager.security.CustomUserDetail;
import com.duyminhdev.cf_manager.security.CustomUserDetailsService;
import com.duyminhdev.cf_manager.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CustomJwtFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public CustomJwtFilter(CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No Bearer token -> skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Already authenticated -> skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            log.debug("Token header found, extracting username");
            
            // 1) Extract username from JWT
            String username = jwtService.extractUsername(token);
            log.debug("Username extracted: {}", username);

            // 2) Load user details (includes active JTI from AccountToken)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.debug("User details loaded for username: {}", username);

            // 3) Validate signature + expiration
            if (!(userDetails instanceof CustomUserDetail cud)) {
                log.warn("UserDetails is not CustomUserDetail instance");
                request.setAttribute("auth_error_code", "AUTH_TOKEN_INVALID");
                request.setAttribute("auth_error_message", "Access token is invalid");
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.validateToken(token, cud)) {
                log.warn("Token signature or expiration validation failed for user: {}", username);
                request.setAttribute("auth_error_code", "AUTH_TOKEN_INVALID");
                request.setAttribute("auth_error_message", "Access token is invalid");
                filterChain.doFilter(request, response);
                return;
            }
            log.debug("Token signature and expiration validated");

            // 4) **CRUCIAL**: JTI check for instant revocation
            String tokenJti = jwtService.extractJti(token);
            String activeJti = cud.getJti();
            log.debug("JTI comparison - tokenJti: {}, activeJti: {}", tokenJti, activeJti);

            if (activeJti == null) {
                log.warn("activeJti is null - no active token found for user: {}", username);
                request.setAttribute("auth_error_code", "AUTH_TOKEN_REVOKED");
                request.setAttribute("auth_error_message", "Access token has been revoked");
                filterChain.doFilter(request, response);
                return;
            }

            if (!activeJti.equals(tokenJti)) {
                log.warn("JWT rejected by JTI check. User: {}, tokenJti: {}, activeJti: {} (mismatch)",
                        username, tokenJti, activeJti);
                request.setAttribute("auth_error_code", "AUTH_TOKEN_REVOKED");
                request.setAttribute("auth_error_message", "Access token has been revoked");
                filterChain.doFilter(request, response);
                return;
            }
            log.debug("JTI validation passed");

            // 5) Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authentication set in SecurityContext for user: {}", username);

        } catch (ExpiredJwtException e) {
            log.warn("Access token expired. path={}, message={}", request.getRequestURI(), e.getMessage());
            request.setAttribute("auth_error_code", "AUTH_TOKEN_EXPIRED");
            request.setAttribute("auth_error_message", "Access token has expired");
        } catch (JwtException e) {
            log.warn("JWT parse/validation failed. path={}, message={}", request.getRequestURI(), e.getMessage());
            request.setAttribute("auth_error_code", "AUTH_TOKEN_INVALID");
            request.setAttribute("auth_error_message", "Access token is invalid");
        } catch (UsernameNotFoundException e) {
            log.warn("User not found for token subject. path={}, message={}", request.getRequestURI(), e.getMessage());
            request.setAttribute("auth_error_code", "AUTH_USER_NOT_FOUND");
            request.setAttribute("auth_error_message", "Authenticated user not found");
        } catch (Exception e) {
            log.error("JWT filter unexpected error. path={}, message={}", request.getRequestURI(), e.getMessage());
            request.setAttribute("auth_error_code", "AUTH_TOKEN_ERROR");
            request.setAttribute("auth_error_message", "Token processing failed");
        }

        filterChain.doFilter(request, response);
    }
}
