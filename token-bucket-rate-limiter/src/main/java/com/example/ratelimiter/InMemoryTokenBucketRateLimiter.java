package com.example.ratelimiter;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryTokenBucketRateLimiter implements TokenBucketRateLimiter {
    private final TokenBucketConfig config;
    private final TimeSource timeSource;
    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public InMemoryTokenBucketRateLimiter(TokenBucketConfig config) {
        this(config, SystemTimeSource.INSTANCE);
    }

    public InMemoryTokenBucketRateLimiter(TokenBucketConfig config, TimeSource timeSource) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource must not be null");
    }

    @Override
    public RateLimitDecision tryAcquire(String key) {
        return tryAcquire(key, 1);
    }

    @Override
    public RateLimitDecision tryAcquire(String key, long permits) {
        validateKey(key);
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }
        if (permits > config.capacity()) {
            throw new IllegalArgumentException("permits must not exceed bucket capacity");
        }

        long nowNanos = timeSource.nanoTime();
        TokenBucket bucket = buckets.computeIfAbsent(key, ignored -> new TokenBucket(config, nowNanos));
        return bucket.tryAcquire(permits, nowNanos);
    }

    @Override
    public void reset(String key) {
        validateKey(key);
        buckets.remove(key);
    }

    @Override
    public int bucketCount() {
        return buckets.size();
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be null or blank");
        }
    }
}
