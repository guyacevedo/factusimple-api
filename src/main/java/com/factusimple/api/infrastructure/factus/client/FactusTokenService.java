package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.dto.FactusAuthResponseDto;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactusTokenService {

    private final TokenRepository tokenRepository;
    private final FactusAuthClient factusAuthClient;

    public String getToken(User user) {
        Optional<Token> tokenOpt = findActiveToken(user);
        if (tokenOpt.isEmpty()) {
            refreshAndSaveToken(user);
            tokenOpt = findActiveToken(user);
        }
        if (tokenOpt.isEmpty()) {
            throw new ApiException(502, "Token no encontrado para Factus");
        }
        return tokenOpt.get().getToken();
    }

    public void refreshAndSaveToken(User user) {
        Optional<Token> refreshTokenOpt = tokenRepository
            .findAllByUserAndRevokedAndTokenType(user, false, Token.TokenType.FACTUS_REFRESH);

        FactusAuthResponseDto response = null;

        if (refreshTokenOpt.isPresent()) {
            try {
                response = factusAuthClient.refreshToken(refreshTokenOpt.get().getToken());
            } catch (Exception e) {
                log.warn("Refresh token inválido o expirado, generando nuevo token: {}", e.getMessage());
                response = factusAuthClient.generateToken();
            }
        } else {
            response = factusAuthClient.generateToken();
        }

        if (response == null || response.getAccess_token() == null) {
            throw new ApiException(502, "No se pudo renovar token Factus");
        }

        tokenRepository.revokeFactusTokensByUser(user);

        LocalDateTime now = LocalDateTime.now();
        tokenRepository.save(Token.builder()
            .user(user)
            .token(response.getAccess_token())
            .tokenType(Token.TokenType.FACTUS_ACCESS)
            .expiresAt(now.plusSeconds(response.getExpires_in()))
            .revoked(false)
            .build());

        if (response.getRefresh_token() != null) {
            tokenRepository.save(Token.builder()
                .user(user)
                .token(response.getRefresh_token())
                .tokenType(Token.TokenType.FACTUS_REFRESH)
                .expiresAt(now.plusDays(30))
                .revoked(false)
                .build());
        }
        log.info("Token Factus renovado para usuario: {}", user.getEmail());
    }

    private Optional<Token> findActiveToken(User user) {
        return tokenRepository.findByUserAndRevokedFalseAndTokenTypeAndExpiresAtAfter(
            user, Token.TokenType.FACTUS_ACCESS, LocalDateTime.now());
    }
}
