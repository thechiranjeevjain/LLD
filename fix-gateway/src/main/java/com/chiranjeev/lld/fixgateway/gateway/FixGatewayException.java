package com.chiranjeev.lld.fixgateway.gateway;

public class FixGatewayException extends RuntimeException {
    public FixGatewayException(String message) {
        super(message);
    }

    public FixGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}

