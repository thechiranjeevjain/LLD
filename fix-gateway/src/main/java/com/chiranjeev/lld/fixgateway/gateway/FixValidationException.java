package com.chiranjeev.lld.fixgateway.gateway;

public final class FixValidationException extends FixGatewayException {
    public FixValidationException(String message) {
        super(message);
    }

    public FixValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

