package com.example.ratelimiter;

public interface TokenBucketRateLimiter {
    RateLimitDecision tryAcquire(String key);

    RateLimitDecision tryAcquire(String key, long permits);

    void reset(String key);

    int bucketCount();
}
