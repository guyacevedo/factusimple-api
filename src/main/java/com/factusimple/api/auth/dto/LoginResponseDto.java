package com.factusimple.api.auth.dto;

import com.factusimple.api.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserResponseDto user;
}
