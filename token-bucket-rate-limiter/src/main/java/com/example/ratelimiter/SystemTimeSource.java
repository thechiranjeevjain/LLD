package com.example.ratelimiter;

enum SystemTimeSource implements TimeSource {
    INSTANCE;

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
