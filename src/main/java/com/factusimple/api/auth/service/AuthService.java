package com.factusimple.api.auth.service;

import com.factusimple.api.auth.dto.*;
import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.exception.UnauthorizedException;
import com.factusimple.api.infrastructure.factus.client.FactusAuthClient;
import com.factusimple.api.infrastructure.factus.dto.FactusAuthResponseDto;
import com.factusimple.api.infrastructure.filter.JwtTokenProvider;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.plan.repository.PlanRepository;
import com.factusimple.api.user.entity.Role;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.mapper.UserMapper;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EstablishmentService establishmentService;
    private final FactusAuthClient factusAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registro de usuario.
     */
    @Transactional
    public LoginResponseDto register(RegisterRequestDto request) {
        createUserLocal(request);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        FactusAuthResponseDto generatedToken = factusAuthClient.generateToken();
        validateFactusResponse(generatedToken);

        Token accessToken = saveTokensLocal(user, generatedToken);
        log.info("Usuario registrado: {}", user.getEmail());

        return buildLoginResponse(user, accessToken);
    }

    @Transactional
    protected void createUserLocal(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Plan defaultPlan = planRepository.findByName("FREE")
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "name", "FREE"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.ESTABLISHMENT)
                .plan(defaultPlan)
                .invoiceCount(0)
                .productsCount(0)
                .customersCount(0)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        establishmentService.createForUser(savedUser, request.getEstablishment());
    }

    /**
     * Inicio de sesion
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        User user = validateCredentialsLocal(request);

        FactusAuthResponseDto generatedToken = factusAuthClient.generateToken();
        validateFactusResponse(generatedToken);

        Token accessToken = revokeAndSaveTokensLocal(user, generatedToken);
        log.info("Usuario logueado: {}", user.getEmail());

        return buildLoginResponse(user, accessToken);
    }

    @Transactional
    protected User validateCredentialsLocal(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Usuario deshabilitado");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    /**
     * Renovar access token usando refresh token.
     */
    @Transactional
    public LoginResponseDto refreshToken(RefreshTokenRequestDto request) {
        Token refreshTokenEntity = validateRefreshTokenLocal(request.getRefreshToken());
        User user = refreshTokenEntity.getUser();

        String factusRefreshToken = refreshTokenEntity.getFactusToken();
        if (factusRefreshToken == null) {
            throw new UnauthorizedException("Token Factus no encontrado");
        }

        FactusAuthResponseDto generatedToken = factusAuthClient.refreshToken(factusRefreshToken);
        validateFactusResponse(generatedToken);

        Token accessToken = saveTokensLocal(user, generatedToken);
        log.info("Token refrescado para usuario: {}", user.getEmail());

        return buildLoginResponse(user, accessToken);
    }

    @Transactional
    protected Token validateRefreshTokenLocal(String refreshToken) {
        Token refreshTokenEntity = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        if (refreshTokenEntity.getTokenType() != Token.TokenType.REFRESH) {
            throw new UnauthorizedException("Token inválido");
        }

        if (!refreshTokenEntity.isValid()) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }

        User user = refreshTokenEntity.getUser();
        Hibernate.initialize(user.getPlan());
        revokeAllUserTokens(user);
        return refreshTokenEntity;
    }

    /**
     * Logout.
     * Revoca todos los tokens asociados al usuario.
     */
    @Transactional
    public void logout(String refreshToken) {
        tokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    User user = token.getUser();
                    revokeAllUserTokens(user);
                    log.info("Usuario deslogueado: {}", user.getEmail());
                });
    }

    /**
     * Revoca todos los tokens activos del usuario.
     */
    @Transactional
    protected void revokeAllUserTokens(User user) {
        tokenRepository.revokeAllByUser(user);
        log.debug("Tokens revocados para usuario: {}", user.getEmail());
    }

    /**
     * Guarda access token y refresh token.
     * Usa JWT interno firmado, almacena token Factus en BD.
     * Retorna el token de acceso para incluir en la respuesta.
     */
    @Transactional
    protected Token saveTokensLocal(User user, FactusAuthResponseDto authResponse) {
        LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusHours(24);

        String internalAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());

        Token accessToken = Token.builder()
                .user(user)
                .token(internalAccessToken)
                .factusToken(authResponse.getAccess_token())
                .tokenType(Token.TokenType.ACCESS)
                .expiresAt(accessTokenExpiresAt)
                .revoked(false)
                .build();

        tokenRepository.save(accessToken);

        String internalRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        Token refreshToken = Token.builder()
                .user(user)
                .token(internalRefreshToken)
                .factusToken(authResponse.getRefresh_token())
                .tokenType(Token.TokenType.REFRESH)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();

        tokenRepository.save(refreshToken);

        return accessToken;
    }

    @Transactional
    protected Token revokeAndSaveTokensLocal(User user, FactusAuthResponseDto authResponse) {
        revokeAllUserTokens(user);
        return saveTokensLocal(user, authResponse);
    }

    /**
     * Valida respuesta de Factus.
     */
    private void validateFactusResponse(FactusAuthResponseDto response) {

        if (response == null
                || response.getAccess_token() == null
                || response.getRefresh_token() == null) {

            throw new UnauthorizedException(
                    "No fue posible generar tokens con Factus"
            );
        }
    }

    /**
     * Construye respuesta login con tokens internos JWT.
     * Nunca expone tokens de Factus al cliente.
     */
    private LoginResponseDto buildLoginResponse(User user, Token accessToken) {
        Token refreshToken = tokenRepository.findLatestRefreshTokenByUser(user)
                .orElseThrow(() -> new UnauthorizedException("Refresh token no encontrado"));

        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), accessToken.getExpiresAt());

        return LoginResponseDto.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresIn(expiresIn)
                .user(userMapper.toDto(user))
                .build();
    }
}