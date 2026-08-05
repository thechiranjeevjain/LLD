package com.example.ratelimiter;

@FunctionalInterface
public interface TimeSource {
    long nanoTime();
}
