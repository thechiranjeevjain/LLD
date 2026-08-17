# LLD Projects

Low-level design interview practice projects in Java. Each project is runnable and has interview notes covering requirements, class diagrams, core flows, design patterns, consistency, extension points, and talking points.

## Study Chapters

- [Design Patterns for Low-Level Design Interviews](docs/DESIGN_PATTERNS_INTERVIEW_CHAPTER.md) — all 23 GoF patterns plus practical Java LLD patterns, comparisons, selection heuristics, coding guidance, and a 45-minute interview workflow.

## Project Index

| Project | Interview problem | Main concepts | LLD notes |
| --- | --- | --- | --- |
| [BookMyShow](book-my-show/README.md) | Movie ticket booking and seat reservation | Seat locking, booking lifecycle, payment adapter, per-show inventory consistency | [LLD](book-my-show/docs/LOW_LEVEL_DESIGN.md) |
| [DesignOrderBook](DesignOrderBook/README.md) | Exchange order book and matching engine | Price-time priority, order lifecycle, matching, snapshots | [LLD](DesignOrderBook/docs/LOW_LEVEL_DESIGN.md) |
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
