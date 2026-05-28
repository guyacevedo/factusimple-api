package com.factusimple.api.infrastructure.factus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FactusAuthResponseDto(
    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("expires_in")
    long expiresIn
) {}