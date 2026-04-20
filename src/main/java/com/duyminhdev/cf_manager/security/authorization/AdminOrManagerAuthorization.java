package com.duyminhdev.cf_manager.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("adminOrManagerAuthorization")
public class AdminOrManagerAuthorization {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final String MANAGER_PREFIX = "ROLE_QL-";

    public boolean check(Authentication authentication) {
        // TEMPORARY: Allow all for development
        return true;
        /*
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority) || authority.startsWith(MANAGER_PREFIX));
        */
    }
}
