package com.example.ratelimiter;

import java.time.Duration;
import java.util.Objects;

public record RateLimitDecision(boolean allowed, long remainingTokens, Duration retryAfter) {
    public RateLimitDecision {
        if (remainingTokens < 0) {
            throw new IllegalArgumentException("remainingTokens must not be negative");
        }
        Objects.requireNonNull(retryAfter, "retryAfter must not be null");
        if (retryAfter.isNegative()) {
            throw new IllegalArgumentException("retryAfter must not be negative");
        }
    }

    static RateLimitDecision allowed(long remainingTokens) {
        return new RateLimitDecision(true, remainingTokens, Duration.ZERO);
    }

    static RateLimitDecision denied(long remainingTokens, Duration retryAfter) {
        return new RateLimitDecision(false, remainingTokens, retryAfter);
    }
}
