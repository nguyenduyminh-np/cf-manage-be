package com.duyminhdev.cf_manager.security;

import com.duyminhdev.cf_manager.entity.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail implements UserDetails {

    private final String username;
    private final String password;
    private final String jti;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetail(Account account, String activeJti) {
        this.username = account.getUsername();
        this.password = account.getPassword();
        this.jti = activeJti;

        // Use RoleCode (e.g. "ADMIN", "QL-002") as the Spring Security authority
        // RoleName is the human-readable Vietnamese label and should NOT be used here
        String roleCode = (account.getRole() != null && account.getRole().getRoleCode() != null)
                ? account.getRole().getRoleCode()
                : "USER";

        String prefixed = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
        this.authorities = List.of(new SimpleGrantedAuthority(prefixed));
    }

    public String getJti() {
        return jti;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
