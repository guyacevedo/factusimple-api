package com.factusimple.api.infrastructure.filter;

import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    public JwtAuthenticationFilter(TokenService tokenService,
                                   UserDetailsService userDetailsService,
                                   TokenRepository tokenRepository) {

        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String jwt = extractJwt(request);

            if (shouldSkipAuthentication(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticate(jwt);

        } catch (Exception e) {

            log.error("Error en autenticación JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el JWT del header Authorization.
     */
    private String extractJwt(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }

    /**
     * Determina si debe omitirse la autenticación.
     */
    private boolean shouldSkipAuthentication(String jwt) {

        return jwt == null
                || SecurityContextHolder.getContext().getAuthentication() != null;
    }

    /**
     * Procesa autenticación completa del JWT.
     */
    private void authenticate(String jwt) {

        if (!tokenService.isTokenValid(jwt)) {

            log.warn("Token NO válido (expirado o mal formado)");
            return;
        }

        Optional<Token> tokenOpt = tokenRepository.findByToken(jwt);

        if (tokenOpt.isEmpty()) {

            log.warn(
                    "Token NO encontrado en BD: {}...",
                    jwt.substring(0, Math.min(20, jwt.length()))
            );

            return;
        }

        Token storedToken = tokenOpt.get();

        if (!isAccessTokenValid(storedToken)) {

            log.warn("Token encontrado pero revocado/expirado");
            return;
        }

        setAuthentication(storedToken);
    }

    /**
     * Valida token persistido.
     */
    private boolean isAccessTokenValid(Token token) {

        return token.isValid()
                && token.getTokenType() == Token.TokenType.ACCESS;
    }

    /**
     * Establece autenticación en Spring Security.
     */
    private void setAuthentication(Token token) {

        String email = token.getUser().getEmail();

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
            log.warn("Intento de autenticación con cuenta deshabilitada: {}", email);
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authToken);

        log.debug("Token validado para usuario: {}", email);
    }
}