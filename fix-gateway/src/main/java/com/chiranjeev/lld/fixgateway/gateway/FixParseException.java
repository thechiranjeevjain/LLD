package com.chiranjeev.lld.fixgateway.gateway;

public final class FixParseException extends FixGatewayException {
    public FixParseException(String message) {
        super(message);
    }

    public FixParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

