package com.factusimple.api.auth.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AuthResponseDto {
    private String token_type;
    private String access_token;
    private String refresh_token;
    private long expires_in;
}
