# Pre-Trade Risk Engine

> **40–60 minute boundary — core fits with representative rules:** Implement the pipeline and two complete rules, then sketch the remaining rules in the same Strategy shape. Atomic reservation and distributed state are discussion topics. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Pluggable Java 17 risk pipeline for quantity, notional, projected exposure, reference-price deviation, and account kill-switch checks.

```powershell
mvn test
mvn exec:java
```

See [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the interview walkthrough.
