package com.example.ratelimiter;

import java.time.Duration;

final class TokenBucket {
    private static final double EPSILON = 1e-9;

    private final long capacity;
    private final double refillTokensPerNano;
    private double availableTokens;
    private long lastRefillNanos;

    TokenBucket(TokenBucketConfig config, long nowNanos) {
        this.capacity = config.capacity();
        this.refillTokensPerNano = (double) config.refillTokens() / config.refillPeriod().toNanos();
        this.availableTokens = config.capacity();
        this.lastRefillNanos = nowNanos;
    }

    synchronized RateLimitDecision tryAcquire(long permits, long nowNanos) {
        refill(nowNanos);

        if (availableTokens + EPSILON >= permits) {
            availableTokens = Math.max(0.0, availableTokens - permits);
            return RateLimitDecision.allowed(wholeTokens());
        }

        return RateLimitDecision.denied(wholeTokens(), retryAfter(permits));
    }

    private void refill(long nowNanos) {
        if (nowNanos <= lastRefillNanos) {
            return;
        }

        long elapsedNanos = nowNanos - lastRefillNanos;
        double tokensToAdd = elapsedNanos * refillTokensPerNano;
        availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
        lastRefillNanos = nowNanos;
    }

    private Duration retryAfter(long permits) {
        double missingTokens = permits - availableTokens;
        long nanosToWait = (long) Math.ceil(missingTokens / refillTokensPerNano);
        return Duration.ofNanos(Math.max(1L, nanosToWait));
    }

    private long wholeTokens() {
        return (long) Math.floor(Math.max(0.0, availableTokens) + EPSILON);
    }
}
