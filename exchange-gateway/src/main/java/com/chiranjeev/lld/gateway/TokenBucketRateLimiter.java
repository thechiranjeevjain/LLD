package com.chiranjeev.lld.gateway;
import java.time.*;import java.util.function.Supplier;
public final class TokenBucketRateLimiter implements RateLimiter {
    private final long capacity,tokensPerSecond;private final Supplier<Instant> clock;private double tokens;private Instant lastRefill;
    public TokenBucketRateLimiter(long capacity,long tokensPerSecond,Supplier<Instant> clock){if(capacity<=0||tokensPerSecond<=0)throw new IllegalArgumentException("positive rates required");this.capacity=capacity;this.tokensPerSecond=tokensPerSecond;this.clock=clock;this.tokens=capacity;this.lastRefill=clock.get();}
    public synchronized boolean tryAcquire(){Instant now=clock.get();tokens=Math.min(capacity,tokens+Duration.between(lastRefill,now).toNanos()/1_000_000_000d*tokensPerSecond);lastRefill=now;if(tokens<1)return false;tokens--;return true;}
}
