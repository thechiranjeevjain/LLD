package com.example.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class InMemoryTokenBucketRateLimiterTest {
    @Test
    void allowsInitialBurstThenRejectsWhenBucketIsEmpty() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(3, 1, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a"), 2);
        assertAllowed(limiter.tryAcquire("client-a"), 1);
        assertAllowed(limiter.tryAcquire("client-a"), 0);

        RateLimitDecision denied = limiter.tryAcquire("client-a");

        assertFalse(denied.allowed());
        assertEquals(0, denied.remainingTokens());
        assertEquals(Duration.ofSeconds(1), denied.retryAfter());
    }

    @Test
    void refillsContinuouslyUsingElapsedTime() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(5, 2, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a", 5), 0);
        assertFalse(limiter.tryAcquire("client-a").allowed());

        timeSource.advance(Duration.ofMillis(250));
        RateLimitDecision halfTokenDecision = limiter.tryAcquire("client-a");

        assertFalse(halfTokenDecision.allowed());
        assertEquals(Duration.ofMillis(250), halfTokenDecision.retryAfter());

        timeSource.advance(Duration.ofMillis(250));
        assertAllowed(limiter.tryAcquire("client-a"), 0);
    }

    @Test
    void keepsBucketsIndependentPerKey() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(2, 1, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a"), 1);
        assertAllowed(limiter.tryAcquire("client-a"), 0);
        assertFalse(limiter.tryAcquire("client-a").allowed());

        assertAllowed(limiter.tryAcquire("client-b"), 1);
        assertEquals(2, limiter.bucketCount());
    }

    @Test
    void supportsMultiPermitRequests() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(10, 10, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a", 4), 6);

        RateLimitDecision denied = limiter.tryAcquire("client-a", 7);
        assertFalse(denied.allowed());
        assertEquals(Duration.ofMillis(100), denied.retryAfter());

        timeSource.advance(Duration.ofMillis(100));
        assertAllowed(limiter.tryAcquire("client-a", 7), 0);
    }

    @Test
    void resetsOneBucketWithoutAffectingOthers() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(1, 1, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a"), 0);
        assertAllowed(limiter.tryAcquire("client-b"), 0);

        limiter.reset("client-a");

        assertAllowed(limiter.tryAcquire("client-a"), 0);
        assertFalse(limiter.tryAcquire("client-b").allowed());
    }

    @Test
    void isThreadSafeForRequestsAgainstTheSameBucket() throws Exception {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(100, 1, Duration.ofDays(1)),
                timeSource
        );
        int requestCount = 1_000;
        AtomicInteger allowedCount = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            tasks.add(() -> {
                await(start);
                if (limiter.tryAcquire("client-a").allowed()) {
                    allowedCount.incrementAndGet();
                }
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(16);
        tasks.forEach(executor::submit);
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(100, allowedCount.get());
    }

    @Test
    void validatesInputs() {
        ManualTimeSource timeSource = new ManualTimeSource();
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(3, 1, Duration.ofSeconds(1)),
                timeSource
        );

        assertThrows(IllegalArgumentException.class, () -> TokenBucketConfig.of(0, 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> TokenBucketConfig.of(1, 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> TokenBucketConfig.of(1, 1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(null));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(" "));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire("client-a", 0));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire("client-a", 4));
    }

    @Test
    void doesNotRefillWhenClockMovesBackward() {
        ManualTimeSource timeSource = new ManualTimeSource();
        timeSource.setNanos(1_000);
        TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(
                TokenBucketConfig.of(2, 1, Duration.ofSeconds(1)),
                timeSource
        );

        assertAllowed(limiter.tryAcquire("client-a"), 1);
        assertAllowed(limiter.tryAcquire("client-a"), 0);

        timeSource.setNanos(0);

        assertFalse(limiter.tryAcquire("client-a").allowed());
    }

    private static void assertAllowed(RateLimitDecision decision, long remainingTokens) {
        assertTrue(decision.allowed());
        assertEquals(remainingTokens, decision.remainingTokens());
        assertEquals(Duration.ZERO, decision.retryAfter());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static final class ManualTimeSource implements TimeSource {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long nanoTime() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }

        void setNanos(long value) {
            nanos.set(value);
        }
    }
}
