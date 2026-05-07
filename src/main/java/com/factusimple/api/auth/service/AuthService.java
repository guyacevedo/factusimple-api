package com.factusimple.api.auth.service;

import com.factusimple.api.auth.dto.*;
import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.exception.UnauthorizedException;
import com.factusimple.api.infrastructure.factus.client.FactusAuthClient;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.plan.repository.PlanRepository;
import com.factusimple.api.user.entity.Role;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.mapper.UserMapper;
import com.factusimple.api.user.repository.RoleRepository;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlanRepository planRepository;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EstablishmentService establishmentService;
    private final FactusAuthClient factusAuthClient;

    /**
     * Registro de usuario.
     */
    public LoginResponseDto register(RegisterRequestDto request) {

        // Validar  email unico
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Rol por defecto: ESTABLISHMENT.
        Role role = roleRepository.findByName("ESTABLISHMENT")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role", "name", "ESTABLISHMENT"));

        // Plan por defecto
        Plan defaultPlan = planRepository.findByName("FREE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan", "name", "FREE"));

        // Crear usuario
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .plan(defaultPlan)
                .invoiceCount(0)
                .productsCount(0)
                .customersCount(0)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado: {}", savedUser.getEmail());

        // Crear el establecimiento
        establishmentService.createForUser(savedUser, request.getEstablishment());

        // Obtener tokens desde Factus
        AuthResponseDto generatedToken = factusAuthClient.generateToken();

        validateFactusResponse(generatedToken);

        // Revocar sesiones anteriores
        revokeAllUserTokens(savedUser);

        // Guardar nuevos tokens
        saveFactusTokens(savedUser, generatedToken);

        return buildLoginResponse(user, generatedToken);
    }

    // Login
    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));

        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }

        // Validar usuario activo
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException(
                    "Usuario deshabilitado"
            );
        }

        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        // Obtener tokens desde Factus
        AuthResponseDto generatedToken = factusAuthClient.generateToken();

        validateFactusResponse(generatedToken);

        // Revocar sesiones anteriores
        revokeAllUserTokens(user);

        // Guardar nuevos tokens
        saveFactusTokens(user, generatedToken);

        log.info("Usuario logueado: {}", user.getEmail());

        return buildLoginResponse(user, generatedToken);
    }

    /**
     * Renovar access token usando refresh token.
     */
    public LoginResponseDto refreshToken(TokenRequestDto request) {

        String refreshToken = request.getRefreshToken();

        Token refreshTokenEntity = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        // Validar tipo
        if (refreshTokenEntity.getTokenType() != Token.TokenType.REFRESH) {
            throw new UnauthorizedException(
                    "Token inválido"
            );
        }

        // Validar estado
        if (!refreshTokenEntity.isValid()) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }

        User user = refreshTokenEntity.getUser();

        // Revocar TODOS los tokens anteriores
        revokeAllUserTokens(user);

        // Solicitar nuevos tokens a Factus
        AuthResponseDto generatedToken = factusAuthClient.refreshToken(refreshToken);

        validateFactusResponse(generatedToken);

        // Guardar nuevos tokens
        saveFactusTokens(user, generatedToken);

        log.info("Token refrescado para usuario: {}", user.getEmail());

        return buildLoginResponse(user, generatedToken);
    }

    /**
     * Logout.
     * Revoca todos los tokens asociados al usuario.
     */
    public void logout(String refreshToken) {
        tokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {

                    User user = token.getUser();

                    revokeAllUserTokens(user);

                    log.info(
                            "Usuario deslogueado: {}",
                            user.getEmail()
                    );
                });
    }

    /**
     * Revoca todos los tokens activos del usuario.
     */
    private void revokeAllUserTokens(User user) {

        tokenRepository.revokeAllByUser(user);

        log.debug(
                "Tokens revocados para usuario: {}",
                user.getEmail()
        );
    }

    /**
     * Guarda access token y refresh token.
     */
    private void saveFactusTokens(User user, AuthResponseDto authResponse) {

        LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusSeconds(authResponse.getExpires_in());

        // Guardar access token
        Token accessToken = Token.builder()
                .user(user)
                .token(authResponse.getAccess_token())
                .tokenType(Token.TokenType.ACCESS)
                .expiresAt(accessTokenExpiresAt)
                .revoked(false)
                .build();

        tokenRepository.save(accessToken);

        // Guardar refresh token
        Token refreshToken = Token.builder()
                .user(user)
                .token(authResponse.getRefresh_token())
                .tokenType(Token.TokenType.REFRESH)
                .expiresAt(LocalDateTime.now().plusDays(30)) // Refresh tokens duran más
                .revoked(false)
                .build();

        tokenRepository.save(refreshToken);
    }

    /**
     * Valida respuesta de Factus.
     */
    private void validateFactusResponse(AuthResponseDto response) {

        if (response == null
                || response.getAccess_token() == null
                || response.getRefresh_token() == null) {

            throw new UnauthorizedException(
                    "No fue posible generar tokens con Factus"
            );
        }
    }

    /**
     * Construye respuesta login.
     */
    private LoginResponseDto buildLoginResponse(
            User user,
            AuthResponseDto token
    ) {

        return LoginResponseDto.builder()
                .accessToken(token.getAccess_token())
                .refreshToken(token.getRefresh_token())
                .expiresIn(token.getExpires_in())
                .user(userMapper.toDto(user))
                .build();
    }
}