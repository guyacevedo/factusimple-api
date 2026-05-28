package com.factusimple.api.infrastructure.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(403, message, "FORBIDDEN");
    }

    public ForbiddenException(String message, String errorCode) {
        super(403, message, errorCode);
    }
}
