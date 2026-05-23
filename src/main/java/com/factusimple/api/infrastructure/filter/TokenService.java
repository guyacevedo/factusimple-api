package com.factusimple.api.infrastructure.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public boolean isTokenValid(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
