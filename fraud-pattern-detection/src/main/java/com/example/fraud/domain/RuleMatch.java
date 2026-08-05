package com.example.fraud.domain;

public record RuleMatch(
        String ruleName,
        int score,
        String reason
) {
    public RuleMatch {
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("ruleName is required");
        }
        if (score <= 0) {
            throw new IllegalArgumentException("score must be positive");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
    }
}
