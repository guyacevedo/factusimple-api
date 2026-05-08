package com.factusimple.api.infrastructure.exception;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(409, message, "CONFLICT");
    }

    public ConflictException(String message, String errorCode) {
        super(409, message, errorCode);
    }
}
