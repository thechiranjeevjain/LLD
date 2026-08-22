# FIX Session Manager

Standalone Java 17 session-layer model with logon, heartbeats, inbound/outbound sequence numbers, resend requests, replay with `PossDup`, duplicate detection, disconnect, and recovery through a persistent `SessionStore` abstraction.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md).
