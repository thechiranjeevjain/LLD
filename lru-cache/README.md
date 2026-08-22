# LRU Cache LLD

Generic, bounded Java 17 LRU cache with `O(1)` get/put/remove, recency updates, eviction callbacks, hit/miss/eviction stats, and a clear single-lock thread-safety strategy.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
