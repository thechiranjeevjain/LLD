package com.example.ratelimiter;

import java.time.Duration;
import java.util.Objects;

public record TokenBucketConfig(long capacity, long refillTokens, Duration refillPeriod) {
    public TokenBucketConfig {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("refillTokens must be positive");
        }
        Objects.requireNonNull(refillPeriod, "refillPeriod must not be null");
        if (refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be positive");
        }
        refillPeriod.toNanos();
    }

    public static TokenBucketConfig of(long capacity, long refillTokens, Duration refillPeriod) {
        return new TokenBucketConfig(capacity, refillTokens, refillPeriod);
    }
}
