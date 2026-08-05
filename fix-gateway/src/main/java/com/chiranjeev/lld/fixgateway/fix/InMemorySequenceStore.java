package com.chiranjeev.lld.fixgateway.fix;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemorySequenceStore implements SequenceStore {
    private final ConcurrentMap<String, SessionSequences> sessions = new ConcurrentHashMap<>();

    @Override
    public int expectedInbound(String sessionId) {
        return sequences(sessionId).expectedInbound.get();
    }

    @Override
    public void markInboundReceived(String sessionId) {
        sequences(sessionId).expectedInbound.incrementAndGet();
    }

    @Override
    public int claimNextOutbound(String sessionId) {
        return sequences(sessionId).nextOutbound.getAndIncrement();
    }

    @Override
    public void reset(String sessionId) {
        sessions.put(sessionId, new SessionSequences());
    }

    private SessionSequences sequences(String sessionId) {
        return sessions.computeIfAbsent(sessionId, ignored -> new SessionSequences());
    }

    private static final class SessionSequences {
        private final AtomicInteger expectedInbound = new AtomicInteger(1);
        private final AtomicInteger nextOutbound = new AtomicInteger(1);
    }
}

