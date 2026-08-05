package com.example.designredis.store;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRedisStoreTest {

    @Test
    void storesReadsAndDeletesStrings() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        store.set("name", "Ada");

        assertEquals(Optional.of("Ada"), store.get("name"));
        assertEquals(Optional.of(RedisType.STRING), store.type("name"));
        assertEquals(1, store.delete("name", "missing"));
        assertEquals(Optional.empty(), store.get("name"));
    }

    @Test
    void expiresKeysByTtl() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        InMemoryRedisStore store = new InMemoryRedisStore(10, clock);

        store.set("token", "abc", Duration.ofSeconds(5));

        assertEquals(5, store.ttlSeconds("token"));
        clock.advance(Duration.ofSeconds(4));
        assertEquals(1, store.ttlSeconds("token"));
        clock.advance(Duration.ofSeconds(1));
        assertEquals(Optional.empty(), store.get("token"));
        assertEquals(-2, store.ttlSeconds("token"));
    }

    @Test
    void expireWithNonPositiveTtlDeletesKey() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        store.set("temporary", "value");

        assertTrue(store.expire("temporary", Duration.ZERO));
        assertEquals(Optional.empty(), store.get("temporary"));
    }

    @Test
    void evictsLeastRecentlyUsedKeyWhenCapacityIsReached() {
        InMemoryRedisStore store = new InMemoryRedisStore(2);

        store.set("a", "1");
        store.set("b", "2");
        assertEquals(Optional.of("1"), store.get("a"));
        store.set("c", "3");

        assertEquals(Optional.of("1"), store.get("a"));
        assertEquals(Optional.empty(), store.get("b"));
        assertEquals(Optional.of("3"), store.get("c"));
    }

    @Test
    void incrementsIntegersAndRejectsNonIntegers() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        assertEquals(1, store.increment("count"));
        assertEquals(2, store.increment("count"));

        store.set("name", "Ada");
        assertThrows(RedisException.class, () -> store.increment("name"));
    }

    @Test
    void rejectsNullValues() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        assertThrows(IllegalArgumentException.class, () -> store.set("name", null));
        assertThrows(IllegalArgumentException.class, () -> store.lpush("queue", "valid", null));
        assertThrows(IllegalArgumentException.class, () -> store.hset("user:1", "name", null));
    }

    @Test
    void supportsListCommandsWithRedisStyleIndexes() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        assertEquals(3, store.lpush("queue", "one", "two", "three"));

        assertEquals(List.of("three", "two", "one"), store.lrange("queue", 0, -1));
        assertEquals(List.of("two", "one"), store.lrange("queue", 1, 10));
        assertEquals(Optional.of("one"), store.rpop("queue"));
        assertEquals(List.of("three", "two"), store.lrange("queue", 0, -1));
    }

    @Test
    void removesListKeyAfterLastPop() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        store.lpush("queue", "only");
        assertEquals(Optional.of("only"), store.rpop("queue"));

        assertEquals(Optional.empty(), store.type("queue"));
    }

    @Test
    void supportsHashCommands() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        assertEquals(1, store.hset("user:1", "name", "Ada"));
        assertEquals(0, store.hset("user:1", "name", "Grace"));
        assertEquals(1, store.hset("user:1", "role", "admin"));

        assertEquals(Optional.of("Grace"), store.hget("user:1", "name"));
        assertEquals(Map.of("name", "Grace", "role", "admin"), store.hgetall("user:1"));
    }

    @Test
    void rejectsWrongTypeOperations() {
        InMemoryRedisStore store = new InMemoryRedisStore(10);

        store.set("name", "Ada");

        assertThrows(RedisException.class, () -> store.lpush("name", "x"));
        assertThrows(RedisException.class, () -> store.hget("name", "field"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
