# Order Management System

> **40–60 minute boundary — whole core fits:** Implement the order aggregate/state machine, OMS facade, compact events/snapshots, and transition tests; discuss durable storage, deduplication, and reconciliation. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Java 17 OMS state machine covering submission, new/reject acknowledgements, partial/full fills, cancel and replace request/ack/reject flows, invalid transitions, and an append-only in-memory event history.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
