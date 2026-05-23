package com.factusimple.api.infrastructure.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret:changeme-with-a-long-random-secret-key-32-chars-minimum}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-hours:24}")
    private long expirationHours;

    public String generateAccessToken(UUID userId, String email) {
        Date expiresAt = new Date(System.currentTimeMillis() + (expirationHours * 60 * 60 * 1000));

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(expiresAt)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.debug("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    public UUID extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String userId = (String) claims.get("userId");
            return userId != null ? UUID.fromString(userId) : null;
        } catch (Exception e) {
            log.debug("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
