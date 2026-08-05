package com.example.designredis.store;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

final class StoredValue {
    private static final long NEVER_EXPIRES = -1;

    private final RedisType type;
    private Object value;
    private long expiresAtMillis = NEVER_EXPIRES;
    private long lastAccessSequence;

    private StoredValue(RedisType type, Object value, long lastAccessSequence) {
        this.type = type;
        this.value = value;
        this.lastAccessSequence = lastAccessSequence;
    }

    static StoredValue string(String value, long lastAccessSequence) {
        return new StoredValue(RedisType.STRING, value, lastAccessSequence);
    }

    static StoredValue list(long lastAccessSequence) {
        return new StoredValue(RedisType.LIST, new LinkedList<String>(), lastAccessSequence);
    }

    static StoredValue hash(long lastAccessSequence) {
        return new StoredValue(RedisType.HASH, new LinkedHashMap<String, String>(), lastAccessSequence);
    }

    RedisType type() {
        return type;
    }

    long lastAccessSequence() {
        return lastAccessSequence;
    }

    void touch(long sequence) {
        this.lastAccessSequence = sequence;
    }

    void expireAt(long expiresAtMillis) {
        this.expiresAtMillis = expiresAtMillis;
    }

    boolean isExpired(long nowMillis) {
        return expiresAtMillis != NEVER_EXPIRES && nowMillis >= expiresAtMillis;
    }

    Optional<Long> ttlMillis(long nowMillis) {
        if (expiresAtMillis == NEVER_EXPIRES) {
            return Optional.empty();
        }
        return Optional.of(Math.max(0, expiresAtMillis - nowMillis));
    }

    String asString() {
        return (String) value;
    }

    void replaceString(String value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    LinkedList<String> asList() {
        return (LinkedList<String>) value;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> asHash() {
        return (Map<String, String>) value;
    }
}
