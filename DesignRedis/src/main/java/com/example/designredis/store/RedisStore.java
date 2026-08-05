package com.example.designredis.store;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RedisStore {
    void set(String key, String value);

    void set(String key, String value, Duration ttl);

    Optional<String> get(String key);

    long delete(String... keys);

    boolean expire(String key, Duration ttl);

    long ttlSeconds(String key);

    long increment(String key);

    Optional<RedisType> type(String key);

    Set<String> keys();

    long lpush(String key, String... values);

    Optional<String> rpop(String key);

    List<String> lrange(String key, int start, int stop);

    long hset(String key, String field, String value);

    Optional<String> hget(String key, String field);

    Map<String, String> hgetall(String key);

    int size();
}
