package com.factusimple.api.shared.dto;

import java.time.Instant;

public record ApiResponseDto<T>(
    boolean success,
    String message,
    String errorCode,
    T data,
    Instant timestamp
) {
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, null, data, Instant.now());
    }

    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        return new ApiResponseDto<>(false, message, errorCode, null, Instant.now());
    }

    public static <T> ApiResponseDto<T> error(String message, String errorCode, T data) {
        return new ApiResponseDto<>(false, message, errorCode, data, Instant.now());
    }
}
