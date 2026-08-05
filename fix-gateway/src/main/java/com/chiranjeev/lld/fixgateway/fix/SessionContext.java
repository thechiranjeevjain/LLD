package com.chiranjeev.lld.fixgateway.fix;

public record SessionContext(String sessionId, String clientCompId, SessionEndpoint outboundEndpoint) {
}

