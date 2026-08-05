package com.chiranjeev.lld.fixgateway.fix;

public record SessionEndpoint(String senderCompId, String targetCompId) {
    public SessionEndpoint {
        if (senderCompId == null || senderCompId.isBlank()) {
            throw new IllegalArgumentException("senderCompId is required");
        }
        if (targetCompId == null || targetCompId.isBlank()) {
            throw new IllegalArgumentException("targetCompId is required");
        }
    }
}

