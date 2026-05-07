package com.factusimple.api.infrastructure.factus.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FactusAuthResponseDto {
    protected String token_type;
    protected String access_token;
    protected String refresh_token;
    protected long expires_in;
}
