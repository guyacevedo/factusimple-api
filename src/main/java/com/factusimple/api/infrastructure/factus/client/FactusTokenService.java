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
        Token stored = tokenOpt.get();
        if (!stored.isValid()) {
            throw new ApiException(502, "Token encontrado pero revocado/expirado");
        }
        return stored.getToken();
    }

    public void refreshAndSaveToken(User user) {
        Optional<Token> refreshTokenOpt = tokenRepository
            .findAllByUserAndRevokedAndTokenType(user, false, Token.TokenType.REFRESH);

        FactusAuthResponseDto response = refreshTokenOpt.isPresent()
            ? factusAuthClient.refreshToken(refreshTokenOpt.get().getToken())
            : factusAuthClient.generateToken();

        if (response == null || response.getAccess_token() == null) {
            throw new ApiException(502, "No se pudo renovar token Factus");
        }

        tokenRepository.revokeAllByUser(user);

        LocalDateTime now = LocalDateTime.now();
        tokenRepository.save(Token.builder()
            .user(user)
            .token(response.getAccess_token())
            .tokenType(Token.TokenType.ACCESS)
            .expiresAt(now.plusSeconds(response.getExpires_in()))
            .revoked(false)
            .build());

        if (response.getRefresh_token() != null) {
            tokenRepository.save(Token.builder()
                .user(user)
                .token(response.getRefresh_token())
                .tokenType(Token.TokenType.REFRESH)
                .expiresAt(now.plusDays(30))
                .revoked(false)
                .build());
        }
        log.info("Token Factus renovado para usuario: {}", user.getEmail());
    }

    private Optional<Token> findActiveToken(User user) {
        return tokenRepository.findAllByUserAndRevokedAndTokenType(
            user, false, Token.TokenType.ACCESS);
    }
}
