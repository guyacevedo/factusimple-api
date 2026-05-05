package com.factusimple.api.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiException extends RuntimeException {

    private int statusCode;
    private String message;
    private String errorCode;

    public ApiException(int statusCode, String message) {
        this(statusCode, message, "UNKNOWN_ERROR");
    }
}