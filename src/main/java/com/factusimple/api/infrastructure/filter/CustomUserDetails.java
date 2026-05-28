package com.factusimple.api.infrastructure.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


import java.util.Collection;
import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CustomUserDetails extends User {

    private final UUID userId;

    public CustomUserDetails(String username, String password,
                             boolean isActive,
                             Collection<? extends GrantedAuthority> authorities,
                             UUID userId) {
        super(username, password, isActive, true, true, isActive, authorities);
        this.userId = userId;
    }
}
