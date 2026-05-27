package com.factusimple.api.infrastructure.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-hours:24}")
    private long expirationHours;

    @PostConstruct
    public void validateConfiguration() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret no configurado. Establezca la variable app.jwt.secret en properties o env variable JWT_SECRET");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret debe tener mínimo 32 caracteres. Actual: " + jwtSecret.length());
        }
        log.info("JWT configuration validated. Secret length: {}", jwtSecret.length());
    }

    public String generateAccessToken(UUID userId, String email) {
        Date expiresAt = new Date(System.currentTimeMillis() + (expirationHours * 60 * 60 * 1000));

        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    public String generateRefreshToken(UUID userId, String email) {
        Date expiresAt = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));

        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    public boolean validateToken(String token) {
        try {
            getAlgorithm().verify(JWT.decode(token));
            return true;
        } catch (JWTVerificationException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
