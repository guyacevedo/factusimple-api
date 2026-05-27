package com.factusimple.api.shared.dto;

import java.time.LocalDateTime;

public record ApiResponseDto<T>(
    boolean success,
    String message,
    String errorCode,
    T data,
    LocalDateTime timestamp
) {
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, null, data, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        return new ApiResponseDto<>(false, message, errorCode, null, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> error(String message, String errorCode, T data) {
        return new ApiResponseDto<>(false, message, errorCode, data, LocalDateTime.now());
    }
}
