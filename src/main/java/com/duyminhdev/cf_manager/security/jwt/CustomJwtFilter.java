package com.duyminhdev.cf_manager.security.jwt;

import com.duyminhdev.cf_manager.security.CustomUserDetail;
import com.duyminhdev.cf_manager.security.CustomUserDetailsService;
import com.duyminhdev.cf_manager.security.JwtService;
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
            // 1) Extract username from JWT
            String username = jwtService.extractUsername(token);

            // 2) Load user details (includes active JTI from AccountToken)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 3) Validate signature + expiration
            if (!(userDetails instanceof CustomUserDetail cud) || !jwtService.validateToken(token, cud)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4) **CRUCIAL**: JTI check for instant revocation
            String tokenJti = jwtService.extractJti(token);
            String activeJti = cud.getJti();

            if (activeJti == null || !activeJti.equals(tokenJti)) {
                log.debug("JWT rejected by JTI check. username={}, tokenJti={}, activeJti={}",
                        username, tokenJti, activeJti);
                filterChain.doFilter(request, response);
                return;
            }

            // 5) Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (UsernameNotFoundException e) {
            log.warn("JWT user not found: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
