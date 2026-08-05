# DesignRedis

DesignRedis is a low-level design implementation of a small Redis-like in-memory data store. It focuses on clean object modeling, deterministic behavior, and runnable tests rather than network protocol compatibility.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, command flow, design patterns, concurrency boundary, and extension points.

## Features

- Thread-safe in-memory key-value store.
- String, list, and hash data types.
- TTL expiry with Redis-style `TTL` responses.
- Lazy cleanup of expired keys.
- Configurable max-key capacity with least-recently-used eviction.
- Command processor and interactive REPL.
- Unit tests for expiry, eviction, type safety, and data structures.

## Design

The implementation separates the API, storage engine, and command layer:

- `RedisStore` defines the command surface used by clients.
- `InMemoryRedisStore` owns key storage, TTL checks, LRU metadata, and type validation.
- `StoredValue` wraps one typed value with expiry and access metadata.
- `CommandProcessor` parses CLI-style commands and delegates to `RedisStore`.
- `RedisApplication` provides a small interactive shell.

The store uses synchronized methods for a simple, explicit concurrency boundary. This keeps compound operations such as type checks, TTL cleanup, value mutation, and LRU updates atomic.

## Supported Commands

```text
SET key value [EX seconds]
GET key
DEL key [key ...]
EXPIRE key seconds
TTL key
INCR key
TYPE key
KEYS
LPUSH key value [value ...]
RPOP key
LRANGE key start stop
HSET key field value
HGET key field
HGETALL key
HELP
EXIT
```

Quoted values are supported:

```text
SET greeting "hello world" EX 60
```

## Run

```bash
mvn test
mvn exec:java
```

You can pass a max-key capacity for quick eviction checks:

```bash
mvn exec:java -Dexec.args="2"
```

Example session:

```text
redis> SET user:1 Alice EX 30
OK
redis> GET user:1
Alice
redis> LPUSH queue first second
(integer) 2
redis> LRANGE queue 0 -1
1) second
2) first
redis> TTL user:1
(integer) 29
```
