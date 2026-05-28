package com.factusimple.api.infrastructure.exception;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(400, message, "BAD_REQUEST");
    }

    public BadRequestException(String message, String errorCode) {
        super(400, message, errorCode);
    }
}
