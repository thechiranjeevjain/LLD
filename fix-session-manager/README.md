# FIX Session Manager

> **40–60 minute boundary — whole core fits:** Implement typed messages, sequence store, session state/actions, expected-sequence cases, resend/replay, and reconnect continuity. Raw FIX codecs and advanced session protocol are discussion topics. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Standalone Java 17 session-layer model with logon, heartbeats, inbound/outbound sequence numbers, resend requests, replay with `PossDup`, duplicate detection, disconnect, and recovery through a persistent `SessionStore` abstraction.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
