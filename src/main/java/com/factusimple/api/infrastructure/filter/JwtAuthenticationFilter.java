package com.factusimple.api.infrastructure.filter;

import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import com.factusimple.api.auth.service.TokenService;
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
                                   TokenRepository refreshTokenRepository) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = refreshTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extraer token JWT del header
            String authHeader = request.getHeader("Authorization");
            String jwt = null;

            // Si el header es: "Bearer <token>"
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }

            // Si aún no hay autenticación en el contexto
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validar token
                if (tokenService.isTokenValid(jwt)) {
                    log.debug("Token válido, buscando en BD...");
                    
                    // Buscar token en la base de datos
                    Optional<Token> tokenOpt = tokenRepository.findByToken(jwt);

                    if (tokenOpt.isPresent()) {
                        Token storedToken = tokenOpt.get();
                        log.debug("Token encontrado en BD, validando...");
                        
                        if (storedToken.isValid() && storedToken.getTokenType() == Token.TokenType.ACCESS) {
                            log.debug("Token válido en BD, cargando usuario...");
                            
                            // Cargar detalles del usuario
                            UserDetails userDetails = userDetailsService.loadUserByUsername(
                                    storedToken.getUser().getEmail()
                            );

                            // Crear token de autenticación
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            // Establecer en el contexto de seguridad
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.debug("Token validado para usuario: {}", storedToken.getUser().getEmail());
                        } else {
                            log.warn("Token encontrado pero no válido (revocado o expirado)");
                        }
                    } else {
                        log.warn("Token NO encontrado en BD: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
                    }
                } else {
                    log.warn("Token NO válido (expirado o mal formado)");
                }
            }
        } catch (Exception e) {
            log.error("Error en autenticación JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
