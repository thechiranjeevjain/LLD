# Splitwise Low-Level Design

This module implements a Splitwise-like expense sharing service in plain Java 17.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, expense flow, design patterns, ledger model, and settlement tradeoffs.

## Requirements Covered

- Create users and groups.
- Add members to groups.
- Add expenses paid by one user and owed by one or more users.
- Support equal, exact, and percentage split modes.
- Maintain pairwise net balances as expenses and payments are recorded.
- Record payments between users to settle or reverse balances.
- Generate simplified settlements by minimizing the number of payments.

## Design

- `SplitwiseService` is the application facade and owns in-memory repositories.
- `SplitStrategy` implementations calculate each participant's owed share.
- `Ledger` is the balance engine. It nets opposite debts immediately.
- `Expense` is immutable transaction history; changing balances happens through ledger events.
- `Money` is a value object that enforces currency consistency and two-decimal precision.

## Run Tests

```powershell
mvn test
```

## Run Demo

```powershell
mvn exec:java
```

## Scope Notes

The design intentionally keeps persistence, authentication, notifications, recurring bills, and real currency conversion outside the core. Those are integration concerns that can be added around the current service and repository boundaries.
