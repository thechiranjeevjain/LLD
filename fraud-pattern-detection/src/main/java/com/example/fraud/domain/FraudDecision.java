package com.example.fraud.domain;

import java.time.Instant;
import java.util.List;

public record FraudDecision(
        String transactionId,
        DecisionType decisionType,
        int riskScore,
        List<RuleMatch> matches,
        Instant evaluatedAt
) {
    public FraudDecision {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId is required");
        }
        if (riskScore < 0 || riskScore > 100) {
            throw new IllegalArgumentException("riskScore must be between 0 and 100");
        }
        matches = List.copyOf(matches);
    }
}
