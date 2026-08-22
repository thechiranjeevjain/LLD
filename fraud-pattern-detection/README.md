# Fraud Pattern Detection

> **40–60 minute boundary — focused slice:** Implement the rule interface, two representative rules, scoring/decision, and engine orchestration. The full rule catalogue and state infrastructure are reference material. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Low-level design sample for detecting risky payment patterns with composable rules.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, rule pipeline, design patterns, consistency model, and production tradeoffs.

## Requirements Covered

- Evaluate each incoming transaction before it is added to fraud state.
- Combine multiple fraud signals into a deterministic risk score.
- Support stateful patterns such as transaction velocity, merchant bursts, and impossible travel.
- Keep rules independently testable and replaceable.
- Preserve a complete decision explanation for customer support and audit review.

## Design

```text
Transaction
    |
    v
FraudDetectionEngine
    |-- FraudStateStore fetches account profile and recent transactions
    |-- FraudRule implementations produce RuleMatch objects
    |-- ScoringPolicy maps score to APPROVE, REVIEW, or BLOCK
    v
FraudDecision
```

The engine depends on `FraudRule` and `FraudStateStore` interfaces, so production code can replace the in-memory store with Redis, Kafka state stores, or a database without changing rule logic.

## Included Rules

- `HighAmountRule`: flags unusually large purchases.
- `VelocityRule`: flags too many account transactions in a short rolling window.
- `MerchantBurstRule`: flags repeated purchases at the same merchant in a short window.
- `ForeignCountryRule`: flags purchases outside the account home country.
- `NewDeviceRule`: flags unknown devices for the account.
- `ImpossibleTravelRule`: flags country changes too close together in time.
- `BlacklistedEntityRule`: blocks blacklisted merchants or IP addresses.

## Run

```powershell
mvn test
mvn -q exec:java
```

If the Maven exec plugin is not installed locally, run the demo directly:

```powershell
mvn -q -DskipTests package
java -cp target/classes com.example.fraud.FraudPatternDetectionDemo
```
