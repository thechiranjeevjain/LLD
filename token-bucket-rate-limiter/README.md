# Token Bucket Rate Limiter

> **40–60 minute boundary — whole core fits:** Implement lazy refill, an injected clock, one bucket/per-key boundary, and deterministic tests; discuss distributed enforcement and cleanup. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Low-level design implementation of a thread-safe, in-memory token bucket rate limiter.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, request flow, token math, design patterns, and concurrency model.

## Requirements

- Allow a burst of requests up to a configured bucket capacity.
- Refill tokens over time at a configured rate.
- Keep independent buckets per rate-limit key, such as user id, API key, or IP address.
- Return a retry-after hint when a request is rejected.
- Be safe when many threads hit the same key concurrently.

## Design

The public entry point is `TokenBucketRateLimiter`.

- `InMemoryTokenBucketRateLimiter` owns a `ConcurrentHashMap<String, TokenBucket>`.
- `computeIfAbsent` creates one bucket per key.
- Each `TokenBucket` synchronizes its own state changes, so different keys do not block each other.
- Refill is lazy: tokens are added only when a request reaches the bucket.
- Time is abstracted behind `TimeSource`, which keeps tests deterministic.

## Token Bucket Rule

Each key has:

- `capacity`: maximum tokens the bucket can hold.
- `refillTokens`: number of tokens added per refill period.
- `refillPeriod`: duration for the refill rate.

On each request:

1. Refill tokens based on elapsed nanoseconds since the last refill.
2. If enough tokens are available, consume the requested permits and allow.
3. Otherwise reject and return the estimated wait time until enough tokens exist.

## Complexity

- `tryAcquire`: `O(1)` average time.
- Space: `O(number_of_distinct_keys)`.
- Concurrency: per-key lock, no global lock on request processing.

## Run

```bash
mvn test
mvn exec:java
```

## Example

```java
TokenBucketConfig config = TokenBucketConfig.of(10, 5, Duration.ofSeconds(1));
TokenBucketRateLimiter limiter = new InMemoryTokenBucketRateLimiter(config);

RateLimitDecision decision = limiter.tryAcquire("user-123");
if (decision.allowed()) {
    // serve request
} else {
    // respond with HTTP 429 and decision.retryAfter()
}
```
