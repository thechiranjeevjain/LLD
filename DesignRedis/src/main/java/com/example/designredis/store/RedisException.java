package com.example.designredis.store;

public final class RedisException extends RuntimeException {
    public RedisException(String message) {
        super(message);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
