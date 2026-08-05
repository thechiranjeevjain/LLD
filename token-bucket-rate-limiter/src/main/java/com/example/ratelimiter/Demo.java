package com.example.ratelimiter;

import java.time.Duration;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucketConfig config = TokenBucketConfig.of(3, 1, Duration.ofSeconds(1));
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(config);
        String key = "api-key-123";

        for (int request = 1; request <= 5; request++) {
            RateLimitDecision decision = limiter.tryAcquire(key);
            System.out.printf(
                    "request=%d allowed=%s remaining=%d retryAfter=%s%n",
                    request,
                    decision.allowed(),
                    decision.remainingTokens(),
                    decision.retryAfter()
            );
        }

        Thread.sleep(1_100);

        RateLimitDecision afterRefill = limiter.tryAcquire(key);
        System.out.printf(
                "after-wait allowed=%s remaining=%d retryAfter=%s%n",
                afterRefill.allowed(),
                afterRefill.remainingTokens(),
                afterRefill.retryAfter()
        );
    }
}
