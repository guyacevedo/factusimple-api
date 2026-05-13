package com.factusimple.api.infrastructure.filter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails extends User {

    private final UUID userId;

    public CustomUserDetails(String username, String password,
                             boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities,
                             UUID userId) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
