# Matching Engine

> **40–60 minute boundary — whole core fits:** Implement order, match result/trade, price-time loop, partial fills, and focused tests; discuss advanced order types, journaling, and partitioning. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Standalone Java 17 implementation of market/limit matching with deterministic price-time priority, maker-price executions, partial fills, and non-resting market remainders.

```powershell
mvn test
mvn exec:java
```

Use [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) as the 40–60 minute interview script.
