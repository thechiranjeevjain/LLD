package com.chiranjeev.lld.fixgateway.fix;

import com.chiranjeev.lld.fixgateway.gateway.FixValidationException;
import com.chiranjeev.lld.fixgateway.gateway.SequenceException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private final String gatewayCompId;
    private final SequenceStore sequenceStore;
    private final FixSerializer serializer;
    private final Set<String> loggedOnSessions = ConcurrentHashMap.newKeySet();

    public SessionManager(String gatewayCompId, SequenceStore sequenceStore, FixSerializer serializer) {
        if (gatewayCompId == null || gatewayCompId.isBlank()) {
            throw new IllegalArgumentException("gatewayCompId is required");
        }
        this.gatewayCompId = gatewayCompId;
        this.sequenceStore = sequenceStore;
        this.serializer = serializer;
    }

    public SessionContext accept(FixMessage inbound) {
        String clientCompId = inbound.require(FixTags.SENDER_COMP_ID);
        String targetCompId = inbound.require(FixTags.TARGET_COMP_ID);
        if (!gatewayCompId.equals(targetCompId)) {
            throw new FixValidationException("Invalid TargetCompID. expected=" + gatewayCompId + ", actual=" + targetCompId);
        }

        String sessionId = sessionId(clientCompId);
        int expectedSequence = sequenceStore.expectedInbound(sessionId);
        int actualSequence = inbound.sequenceNumber();
        if (actualSequence != expectedSequence) {
            throw new SequenceException(sessionId, expectedSequence, actualSequence);
        }

        sequenceStore.markInboundReceived(sessionId);
        return new SessionContext(sessionId, clientCompId, new SessionEndpoint(gatewayCompId, clientCompId));
    }

    public void markLoggedOn(SessionContext context) {
        loggedOnSessions.add(context.sessionId());
    }

    public void ensureLoggedOn(SessionContext context) {
        if (!loggedOnSessions.contains(context.sessionId())) {
            throw new FixValidationException("Session is not logged on: " + context.sessionId());
        }
    }

    public String serialize(SessionContext context, FixMessage outbound) {
        int sequenceNumber = sequenceStore.claimNextOutbound(context.sessionId());
        return serializer.serialize(context.outboundEndpoint(), sequenceNumber, outbound);
    }

    private String sessionId(String clientCompId) {
        return clientCompId + "->" + gatewayCompId;
    }
}

