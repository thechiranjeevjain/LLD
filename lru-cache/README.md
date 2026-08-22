# LRU Cache LLD

> **40–60 minute boundary — whole core fits:** Implement the cache, private node, map/list operations, and focused tests; omit the demo and production extensions. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Generic, bounded Java 17 LRU cache with `O(1)` get/put/remove, recency updates, eviction callbacks, hit/miss/eviction stats, and a clear single-lock thread-safety strategy.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
