package com.factusimple.api.infrastructure.filter;

import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                user.getId()
        );
    }
}