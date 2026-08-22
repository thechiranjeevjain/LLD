# Exchange Gateway

> **40–60 minute boundary — orchestration core fits:** Implement one protocol adapter, transport/rate-limit ports, gateway queue/reconnect flow, and event mapping. Reuse or discuss the second adapter and token-bucket internals. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Standalone Java 17 outbound exchange gateway with pluggable FIX and OUCH-like adapters, token-bucket throttling, acknowledgement/execution decoding, disconnect queuing, and throttled reconnect flushing.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
