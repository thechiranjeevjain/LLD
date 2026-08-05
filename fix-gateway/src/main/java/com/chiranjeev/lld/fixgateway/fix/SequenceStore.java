package com.chiranjeev.lld.fixgateway.fix;

public interface SequenceStore {
    int expectedInbound(String sessionId);

    void markInboundReceived(String sessionId);

    int claimNextOutbound(String sessionId);

    void reset(String sessionId);
}

