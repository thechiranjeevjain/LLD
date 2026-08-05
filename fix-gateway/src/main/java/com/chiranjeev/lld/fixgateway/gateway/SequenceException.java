package com.chiranjeev.lld.fixgateway.gateway;

public final class SequenceException extends FixGatewayException {
    public SequenceException(String sessionId, int expected, int actual) {
        super("Sequence mismatch for " + sessionId + ". expected=" + expected + ", actual=" + actual);
    }
}

