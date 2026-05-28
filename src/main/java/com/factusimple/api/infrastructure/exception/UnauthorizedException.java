package com.factusimple.api.infrastructure.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(401, message, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super(401, "No autorizado", "UNAUTHORIZED");
    }
}