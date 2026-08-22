# LLD Projects

Low-level design interview practice projects in Java. Each project is runnable and has interview notes covering requirements, class diagrams, core flows, design patterns, consistency, extension points, and talking points.

## Study Chapters

- [Design Patterns for Low-Level Design Interviews](docs/DESIGN_PATTERNS_INTERVIEW_CHAPTER.md) — all 23 GoF patterns plus practical Java LLD patterns, comparisons, selection heuristics, coding guidance, and a 45-minute interview workflow.
- [Trading Projects: Relationship, Redundancy, and Study Guide](docs/TRADING_PROJECT_RELATIONSHIP_GUIDE.md) — separates canonical 40–60 minute LLD answers from production-shaped counterparts, explains intentional overlap, and defines when a design should graduate into system design.

## Project Index

| Project | Interview problem | Main concepts | LLD notes |
| --- | --- | --- | --- |
| [BookMyShow](book-my-show/README.md) | Movie ticket booking and seat reservation | Seat locking, booking lifecycle, payment adapter, per-show inventory consistency | [LLD](book-my-show/docs/LOW_LEVEL_DESIGN.md) |
| [DesignOrderBook](DesignOrderBook/README.md) | Exchange order book and matching engine | Price-time priority, order lifecycle, matching, snapshots | [LLD](DesignOrderBook/docs/LOW_LEVEL_DESIGN.md) |
| [Matching Engine](matching-engine/README.md) | Core market/limit matching | Price-time priority, maker-price trades, partial fills | [LLD](matching-engine/docs/LOW_LEVEL_DESIGN.md) |
| [Pre-Trade Risk Engine](pre-trade-risk-engine/README.md) | Pluggable order risk checks | Quantity, notional, exposure, price bands, kill switch | [LLD](pre-trade-risk-engine/docs/LOW_LEVEL_DESIGN.md) |
| [Order Management System](order-management-system/README.md) | Order lifecycle and venue events | State machine, fills, cancel/replace, audit events | [LLD](order-management-system/docs/LOW_LEVEL_DESIGN.md) |
| [FIX Session Manager](fix-session-manager/README.md) | FIX session reliability | Logon, heartbeat, sequencing, resend/replay, reconnect | [LLD](fix-session-manager/docs/LOW_LEVEL_DESIGN.md) |
| [Exchange Gateway](exchange-gateway/README.md) | Outbound venue connectivity | FIX/OUCH-like adapters, throttling, executions, reconnect | [LLD](exchange-gateway/docs/LOW_LEVEL_DESIGN.md) |
| [LRU Cache](lru-cache/README.md) | Extensible bounded cache | Hash map + linked list, O(1) eviction, locking | [LLD](lru-cache/docs/LOW_LEVEL_DESIGN.md) |
| [DesignRedis](DesignRedis/README.md) | Redis-like in-memory data store | Typed values, TTL, LRU eviction, command processor, synchronized store | [LLD](DesignRedis/docs/LOW_LEVEL_DESIGN.md) |
| [Elevator System](elevator-system/README.md) | Elevator dispatch and movement simulation | Strategy scheduling, car state machine, stop queues, snapshots | [LLD](elevator-system/docs/LOW_LEVEL_DESIGN.md) |
| [FIX Gateway](fix-gateway/README.md) | FIX order gateway | Parser/serializer, session sequencing, mapper, risk rules, exchange adapter | [LLD](fix-gateway/docs/LLD.md) |
| [Fraud Pattern Detection](fraud-pattern-detection/README.md) | Rule-based payment fraud engine | Rule pipeline, scoring policy, state store, explainable decisions | [LLD](fraud-pattern-detection/docs/LOW_LEVEL_DESIGN.md) |
| [Parking Lot](parking-lot/README.md) | Parking allocation, ticketing, and payment | Allocation strategy, pricing strategy, ticket lifecycle, spot compatibility | [LLD](parking-lot/docs/LOW_LEVEL_DESIGN.md) |
| [Splitwise](splitwise/README.md) | Expense sharing and settlement | Split strategies, ledger, money value object, debt simplification | [LLD](splitwise/docs/LOW_LEVEL_DESIGN.md) |
| [Token Bucket Rate Limiter](token-bucket-rate-limiter/README.md) | Per-key rate limiting | Token bucket math, lazy refill, per-key locking, retry-after | [LLD](token-bucket-rate-limiter/docs/LOW_LEVEL_DESIGN.md) |
| [URL Shortener](url-shortener/README.md) | URL shortening and redirects | REST layers, MySQL source of truth, Redis cache-aside, Base62 codes | [LLD](url-shortener/docs/LOW_LEVEL_DESIGN.md) |

## Interview Prep Checklist

For each project, review:

- Requirements and non-goals.
- Core classes and ownership boundaries.
- Mermaid class diagram.
- Main workflow sequence diagram or flowchart.
- Design pattern explanations.
- Data consistency and concurrency assumptions.
- Extension points and production tradeoffs.

## Running Projects

Most projects are Maven modules and can be tested from their own directory:

```bash
mvn test
```

The `url-shortener` project is a Spring Boot service and also includes Docker Compose for MySQL and Redis.
