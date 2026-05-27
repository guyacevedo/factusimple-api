package com.factusimple.api.auth.dto;

import com.factusimple.api.user.dto.UserResponseDto;

public record LoginResponseDto(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    UserResponseDto user
) {}
