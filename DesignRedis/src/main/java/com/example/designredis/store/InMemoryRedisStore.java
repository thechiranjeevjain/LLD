package com.example.designredis.store;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class InMemoryRedisStore implements RedisStore {
    private final Map<String, StoredValue> data = new HashMap<>();
    private final int maxKeys;
    private final Clock clock;
    private long accessSequence;

    public InMemoryRedisStore(int maxKeys) {
        this(maxKeys, Clock.systemUTC());
    }

    public InMemoryRedisStore(int maxKeys, Clock clock) {
        if (maxKeys <= 0) {
            throw new IllegalArgumentException("maxKeys must be positive");
        }
        this.maxKeys = maxKeys;
        this.clock = clock;
    }

    @Override
    public synchronized void set(String key, String value) {
        validateKey(key);
        validateValue(value, "value");
        StoredValue storedValue = StoredValue.string(value, nextAccessSequence());
        putNewOrReplace(key, storedValue);
    }

    @Override
    public synchronized void set(String key, String value, Duration ttl) {
        validateKey(key);
        validateValue(value, "value");
        validatePositiveTtl(ttl);
        StoredValue storedValue = StoredValue.string(value, nextAccessSequence());
        storedValue.expireAt(nowMillis() + ttl.toMillis());
        putNewOrReplace(key, storedValue);
    }

    @Override
    public synchronized Optional<String> get(String key) {
        StoredValue value = getExisting(key, RedisType.STRING);
        if (value == null) {
            return Optional.empty();
        }
        value.touch(nextAccessSequence());
        return Optional.of(value.asString());
    }

    @Override
    public synchronized long delete(String... keys) {
        long deleted = 0;
        for (String key : keys) {
            validateKey(key);
            if (data.remove(key) != null) {
                deleted++;
            }
        }
        return deleted;
    }

    @Override
    public synchronized boolean expire(String key, Duration ttl) {
        validateKey(key);
        StoredValue value = getExisting(key);
        if (value == null) {
            return false;
        }

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            data.remove(key);
            return true;
        }

        value.expireAt(nowMillis() + ttl.toMillis());
        value.touch(nextAccessSequence());
        return true;
    }

    @Override
    public synchronized long ttlSeconds(String key) {
        validateKey(key);
        StoredValue value = getExisting(key);
        if (value == null) {
            return -2;
        }

        Optional<Long> ttlMillis = value.ttlMillis(nowMillis());
        if (ttlMillis.isEmpty()) {
            value.touch(nextAccessSequence());
            return -1;
        }

        value.touch(nextAccessSequence());
        return Math.max(0, (long) Math.ceil(ttlMillis.get() / 1000.0));
    }

    @Override
    public synchronized long increment(String key) {
        validateKey(key);
        StoredValue value = getExisting(key, RedisType.STRING);
        if (value == null) {
            StoredValue storedValue = StoredValue.string("1", nextAccessSequence());
            putNewOrReplace(key, storedValue);
            return 1;
        }

        long current;
        try {
            current = Long.parseLong(value.asString());
        } catch (NumberFormatException exception) {
            throw new RedisException("value is not an integer", exception);
        }

        long next;
        try {
            next = Math.addExact(current, 1);
        } catch (ArithmeticException exception) {
            throw new RedisException("increment would overflow", exception);
        }
        value.replaceString(Long.toString(next));
        value.touch(nextAccessSequence());
        return next;
    }

    @Override
    public synchronized Optional<RedisType> type(String key) {
        validateKey(key);
        StoredValue value = getExisting(key);
        if (value == null) {
            return Optional.empty();
        }
        value.touch(nextAccessSequence());
        return Optional.of(value.type());
    }

    @Override
    public synchronized Set<String> keys() {
        purgeExpiredKeys();
        List<String> sorted = new ArrayList<>(data.keySet());
        sorted.sort(String::compareTo);
        return new LinkedHashSet<>(sorted);
    }

    @Override
    public synchronized long lpush(String key, String... values) {
        validateKey(key);
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("at least one list value is required");
        }
        for (String value : values) {
            validateValue(value, "list value");
        }

        StoredValue storedValue = getExisting(key, RedisType.LIST);
        if (storedValue == null) {
            storedValue = StoredValue.list(nextAccessSequence());
            putNewOrReplace(key, storedValue);
        }

        LinkedList<String> list = storedValue.asList();
        for (String value : values) {
            list.addFirst(value);
        }
        storedValue.touch(nextAccessSequence());
        return list.size();
    }

    @Override
    public synchronized Optional<String> rpop(String key) {
        validateKey(key);
        StoredValue value = getExisting(key, RedisType.LIST);
        if (value == null) {
            return Optional.empty();
        }

        LinkedList<String> list = value.asList();
        if (list.isEmpty()) {
            data.remove(key);
            return Optional.empty();
        }

        String removed = list.removeLast();
        if (list.isEmpty()) {
            data.remove(key);
        } else {
            value.touch(nextAccessSequence());
        }
        return Optional.of(removed);
    }

    @Override
    public synchronized List<String> lrange(String key, int start, int stop) {
        validateKey(key);
        StoredValue value = getExisting(key, RedisType.LIST);
        if (value == null) {
            return List.of();
        }

        List<String> list = value.asList();
        value.touch(nextAccessSequence());

        int size = list.size();
        int normalizedStart = normalizeIndex(start, size);
        int normalizedStop = normalizeIndex(stop, size);
        if (normalizedStart < 0) {
            normalizedStart = 0;
        }
        if (normalizedStop >= size) {
            normalizedStop = size - 1;
        }
        if (normalizedStart > normalizedStop || normalizedStart >= size || normalizedStop < 0) {
            return List.of();
        }

        return List.copyOf(list.subList(normalizedStart, normalizedStop + 1));
    }

    @Override
    public synchronized long hset(String key, String field, String value) {
        validateKey(key);
        validateField(field);
        validateValue(value, "hash value");

        StoredValue storedValue = getExisting(key, RedisType.HASH);
        if (storedValue == null) {
            storedValue = StoredValue.hash(nextAccessSequence());
            putNewOrReplace(key, storedValue);
        }

        Map<String, String> hash = storedValue.asHash();
        boolean isNew = !hash.containsKey(field);
        hash.put(field, value);
        storedValue.touch(nextAccessSequence());
        return isNew ? 1 : 0;
    }

    @Override
    public synchronized Optional<String> hget(String key, String field) {
        validateKey(key);
        validateField(field);

        StoredValue value = getExisting(key, RedisType.HASH);
        if (value == null) {
            return Optional.empty();
        }

        value.touch(nextAccessSequence());
        return Optional.ofNullable(value.asHash().get(field));
    }

    @Override
    public synchronized Map<String, String> hgetall(String key) {
        validateKey(key);
        StoredValue value = getExisting(key, RedisType.HASH);
        if (value == null) {
            return Map.of();
        }

        value.touch(nextAccessSequence());
        return new LinkedHashMap<>(value.asHash());
    }

    @Override
    public synchronized int size() {
        purgeExpiredKeys();
        return data.size();
    }

    private void putNewOrReplace(String key, StoredValue storedValue) {
        boolean isNewKey = !data.containsKey(key);
        if (isNewKey) {
            ensureCapacityForNewKey();
        }
        data.put(key, storedValue);
    }

    private StoredValue getExisting(String key) {
        StoredValue value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value.isExpired(nowMillis())) {
            data.remove(key);
            return null;
        }
        return value;
    }

    private StoredValue getExisting(String key, RedisType expectedType) {
        StoredValue value = getExisting(key);
        if (value == null) {
            return null;
        }
        if (value.type() != expectedType) {
            throw new RedisException("wrong type: expected " + expectedType.wireName() + " but found " + value.type().wireName());
        }
        return value;
    }

    private void ensureCapacityForNewKey() {
        purgeExpiredKeys();
        if (data.size() < maxKeys) {
            return;
        }

        String leastRecentlyUsedKey = data.entrySet()
                .stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().lastAccessSequence()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("cannot evict from an empty store"));
        data.remove(leastRecentlyUsedKey);
    }

    private void purgeExpiredKeys() {
        long now = nowMillis();
        data.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private long nextAccessSequence() {
        accessSequence++;
        return accessSequence;
    }

    private long nowMillis() {
        return clock.millis();
    }

    private static int normalizeIndex(int index, int size) {
        if (index < 0) {
            return size + index;
        }
        return index;
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
    }

    private static void validateField(String field) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
    }

    private static void validateValue(String value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    private static void validatePositiveTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }
}
