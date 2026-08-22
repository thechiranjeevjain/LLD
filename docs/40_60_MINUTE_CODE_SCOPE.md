# Strict 40–60 Minute LLD Code Scope

## The rule

The repositories in `LLDProjects` are runnable reference implementations, not transcripts that must be reproduced line for line in an interview. A complete learning repository may include demos, repositories, adapters, extra strategies, exception hierarchies, configuration, and exhaustive tests. Those files are useful for learning but are not part of the one-hour coding promise.

For a 40–60 minute interview, target:

- 4–8 important types; tiny enums/records may be nested or sketched.
- Roughly 120–220 readable lines of core logic, not counting imports, trivial accessors, or tests.
- One aggregate that clearly owns mutable state.
- One end-to-end critical path.
- One invalid or racing path.
- Two to four focused tests or test cases.
- Complexity, concurrency, and production extensions explained rather than implemented.

If the core works and those points are covered, stop. Do not add controllers, databases, Docker, Kafka, authentication, dashboards, distributed locks, full wire protocols, or every strategy during the interview.

## Scope classifications

- **Whole core fits:** The domain implementation is compact enough to use almost completely; omit demo and exhaustive tests.
- **Focused slice required:** The runnable project is deliberately broader than one hour. Implement only the named files/behaviors below.
- **Integration review:** The project combines multiple questions. Use it after the focused projects; never promise to recreate it fully in one hour.

## 1. Parking Lot — focused slice required

**Write:** `Vehicle`, `ParkingSpot`, `Ticket`, `ParkingFloor`, `SpotAllocationStrategy`, and `ParkingLotService`. Implement park, locate compatible available spot, mark occupied, unpark, and reject full/duplicate parking.

**Sketch or discuss:** pricing, payment processing, receipts, the full exception hierarchy, multiple allocation strategies, persistence, and concurrency across entrances.

**Stop when:** one vehicle can park/unpark atomically, an incompatible/full request fails without mutation, and allocation Strategy is visible.

## 2. LRU Cache — whole core fits

**Write:** `Cache`, `LruCache`, its private `Node`, and optionally `EvictionListener`. Implement get, put/update, remove, move-to-front, and tail eviction.

**Sketch or discuss:** statistics, listener failure policy, TTL, loading, weighted capacity, segmentation, and distributed caching.

**Stop when:** map/list invariants are correct, get updates recency, capacity eviction works in `O(1)`, and the lock boundary is stated.

## 3. Splitwise — focused slice required

**Write:** `Money`, `SplitInput`, `SplitStrategy`, one concrete strategy such as `EqualSplitStrategy`, `Expense`, `Ledger`, and a small `SplitwiseService`. Implement add expense and update pairwise balances.

**Sketch or discuss:** exact and percentage strategies, groups, debt simplification, settlement history, persistence, currency conversion, and notifications.

**Stop when:** shares sum exactly to the expense, invalid input is rejected before ledger mutation, and balances are explainable.

## 4. Elevator System — focused slice required

**Write:** `ElevatorCar`, `ElevatorController`, `ElevatorSelectionStrategy`, `NearestCarSelectionStrategy`, and small direction/status/call value types. Implement hall-call assignment and one movement step.

**Sketch or discuss:** door timing, emergency/fire modes, maintenance, destination dispatch, starvation prevention, multi-building control, and telemetry.

**Stop when:** the controller selects an eligible car, the car owns its stop queue/state, and full or direction-incompatible cars are handled.

## 5. Token Bucket Rate Limiter — whole core fits

**Write:** `TokenBucket`, `TokenBucketConfig`, `TimeSource`, `RateLimitDecision`, and a small per-key limiter. Implement lazy refill and `tryAcquire` with an explicit synchronization boundary.

**Sketch or discuss:** cleanup of idle keys, distributed coordination, Redis/Lua, clock anomalies, configuration refresh, metrics, and multiple permit weights.

**Stop when:** the refill formula is correct, tests use an injected clock rather than sleeping, and capacity is never exceeded under concurrency.

## 6. BookMyShow — focused slice required

**Write:** `ShowInventory`, `Booking`, `BookingStatus`, `BookingService`, and `PaymentGateway`. Implement reserve selected seats, confirm after payment, and release on failure.

**Sketch or discuss:** movie/theatre catalogue, repositories, money/payment records, search APIs, hold expiry scheduler, database locking, distributed transactions, and notifications.

**Stop when:** two requests cannot confirm the same seat, multi-seat reservation is all-or-nothing, and payment failure restores inventory.

## 7. Fraud Pattern Detection — focused slice required

**Write:** `Transaction`, `FraudContext`, `FraudRule`, two representative rules, `ScoringPolicy`, `FraudDecision`, and `FraudDetectionEngine`.

**Sketch or discuss:** the remaining rule catalogue, durable state store, feature ingestion, model scoring, rule configuration, false-positive feedback, audit retention, and streaming infrastructure.

**Stop when:** rules are independently pluggable, the decision contains reasons, and state freshness/ordering is acknowledged.

## 8. DesignRedis — focused slice required

**Write:** a reduced `RedisStore`, `StoredValue`, and `InMemoryRedisStore` supporting only SET, GET, DEL, and TTL. Reuse the LRU Cache explanation rather than reimplementing every eviction detail if time is tight.

**Sketch or discuss:** lists/hashes, command parser, REPL, all Redis error cases, active expiry, persistence, replication, clustering, transactions, pub/sub, and real RESP networking.

**Stop when:** type/value ownership is clear, expired keys are never returned, compound mutation is atomic, and expiry versus eviction is distinguished.

## 9. URL Shortener — focused slice required

**Write:** `ShortLink`, `Base62CodeGenerator`, repository and cache ports, and `ShortLinkService` with create and resolve operations. Use in-memory fakes or interfaces.

**Sketch or discuss:** Spring controllers, DTOs, exception handler, JPA annotations, Redis client code, configuration classes, Docker, authentication, analytics, abuse controls, and deployment.

**Stop when:** code generation/collision retry is correct, database is the source of truth, redirect uses cache-aside, and expiry/not-found behavior is explicit.

## 10. Order Management System — whole core fits

**Write:** `OrderStatus`, `OmsOrder`, `OrderManagementSystem`, and compact snapshot/event records. Implement submit, new acknowledgement, fill, cancel request/ack, replace request/ack, and transition validation.

**Sketch or discuss:** persistence, execution-ID deduplication, out-of-order events, optimistic locking, reconciliation, outbox, APIs, and reporting projections.

**Stop when:** partial/full fill arithmetic is correct, fills may race pending requests, terminal states reject mutation, and an audit event is emitted.

## 11. Pre-Trade Risk Engine — whole core fits with representative rules

**Write:** `OrderRequest`, `RiskContext`, `RiskLimits`, `RiskRule`, `RiskEngine`, `RiskDecision`, and two complete representative rules. Show the remaining quantity/notional/exposure/price/kill-switch rules as the same Strategy shape if time remains.

**Sketch or discuss:** atomic exposure reservation, versioned snapshots, durable limits, portfolio Greeks, market-data freshness, fail-open/fail-closed policy, audit, and distributed state.

**Stop when:** an accepted order and a multi-rule rejection are explainable, integer units/overflow are addressed, and the stale-exposure race is named.

## 12. DesignOrderBook — focused slice required

**Write:** `Order`, `OrderRequest`, `OrderBook`, `Trade`, and small side/type/status value types. Implement add/rest, match or partial fill, cancel, best bid/ask, and cancel-replace if time remains.

**Sketch or discuss:** multi-symbol `MatchingEngine` facade, full depth snapshots, every time-in-force, persistence/journal, replay, self-trade prevention, auctions, stop/iceberg orders, and market-data publication.

**Stop when:** best price and FIFO are preserved, integer price/quantity is used, filled orders leave the active index, and complexity is stated.

## 13. Matching Engine — whole core fits

**Write:** `Order`, `MatchingEngine`, `Trade`, `MatchResult`, and side/type enums. Implement market/limit crossing, maker-price trades, best-price then FIFO matching, partial fills, and resting limit remainder.

**Sketch or discuss:** cancel/replace storage mechanics, IOC/FOK, self-trade prevention, auctions, symbol partitioning, journaling, replay, multicast market data, and latency engineering.

**Stop when:** a multi-level match is deterministic, market remainder does not rest, and ownership between Matching Engine and Order Book is explained.

## 14. FIX Session Manager — whole core fits

**Write:** `FixMessage`, `SessionStore`, `FixSessionManager`, and compact state/action types. Implement logon, heartbeat scheduling, expected sequence handling, resend request, duplicate suppression, replay, and disconnect/reconnect state.

**Sketch or discuss:** raw FIX parsing/checksum, TestRequest timeout, SequenceReset/GapFill, session calendar, durable raw-message journal, TLS, authentication, and exact-byte replay.

**Stop when:** equal/low/high inbound sequence cases are correct, outbound replay preserves sequence identity, and required recovery state is explicit.

## 15. Exchange Gateway — whole orchestration core fits; choose one wire adapter

**Write:** `InternalOrder`, `ProtocolAdapter`, one FIX-like or OUCH-like adapter, `ExchangeTransport`, `RateLimiter`, `VenueEvent`, and `ExchangeGateway`. Implement submit, throttle/queue, reconnect flush, and acknowledgement/fill translation.

**Sketch or reuse:** the second protocol adapter and token-bucket internals. Discuss bounded queues, cancel priority, durable outbox, sent-but-unacknowledged reconciliation, session ownership, conformance, and networking.

**Stop when:** internal domain types do not leak wire tags, one order survives disconnect/throttle, and duplicate-send risk is explained.

## 16. FIX Gateway — integration review, not a one-hour full build

**Write only after clarifying the interviewer's emphasis:** either (a) `FixMessage` plus minimal parser/serializer and session sequence validation, or (b) `OrderFixMapper` plus `FixGateway` orchestration using stubbed session, risk, and router ports. Do not attempt both complete halves.

**Read/discuss:** checksum/body-length details, complete domain model, all exceptions, duplicate tracker, quantity/notional rules, in-memory exchange, demo, and exhaustive parser/gateway tests.

**Stop when:** one NewOrderSingle becomes one internal order and one execution report, component boundaries are clear, and the focused Risk/Session/Exchange Gateway projects are named for deeper questions.

## Final interview red flags

- Claiming the entire runnable repository can be recreated in one hour.
- Writing framework or infrastructure boilerplate before the domain core.
- Producing many tiny classes without one clear state owner.
- Applying patterns without a requirement that needs them.
- Ignoring invalid transitions, partial progress, duplicates, or ordering.
- Saying “thread-safe” because one collection is concurrent while compound state is not atomic.
- Calling a learning project production-ready because it includes Docker or Kubernetes.
- Continuing after the required core, tests, complexity, and trade-offs are already complete.

The honest interview statement is: “This repository is the runnable reference. In the interview I would implement the named core slice, test its invariants, and discuss the remaining files as extensions.”
