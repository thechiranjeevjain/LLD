# LLD Learning Order: One Exhaustive ROI Ranking

This is the single canonical learning order for all 16 runnable Maven projects in `G:\TechStudyNotes\LLDProjects`. Follow it from top to bottom. The ranking optimizes for prerequisite flow, reusable interview concepts, and return on limited time and mental energy—not alphabetical order or project size.

Use [Strict 40–60 Minute LLD Code Scope](40_60_MINUTE_CODE_SCOPE.md) to decide what code to write versus discuss for each ranked project. The full runnable repository is never the required one-hour output unless that scope guide explicitly says its core fits.

For every project, use the same learning loop: read its README and LLD document, draw the design from memory, read the tests as executable requirements, trace the core implementation, run the tests and demo, then explain the design aloud in 40–60 minutes. Move forward only when you meet the stated completion gate.

1. **[Parking Lot](../parking-lot/README.md)** — foundational object modelling

   - **Why first:** It has the lowest prerequisite burden and teaches the basic skill every later LLD needs: turning nouns and operations into objects with clear responsibilities.
   - **Learn:** entities versus services, spot compatibility, allocation Strategy, pricing Strategy, ticket lifecycle, validation boundaries, and avoiding one giant manager class.
   - **Completion gate:** Draw the classes, park and unpark one vehicle, reject an incompatible/full allocation, and explain how a new vehicle or pricing type is added.
   - **Unlocks:** Splitwise, Elevator, BookMyShow, OMS, and every stateful domain model.

2. **[LRU Cache](../lru-cache/README.md)** — canonical data-structure LLD

   - **Why here:** It adds hard invariants and complexity reasoning without introducing a large business domain. It is one of the highest-ROI coding interview designs.
   - **Learn:** hash map plus doubly linked list, sentinel nodes, `O(1)` get/put/remove, recency mutation, eviction callbacks, and a single-lock linearizability boundary.
   - **Completion gate:** Implement it from a blank file, explain every pointer update, prove `O(1)` operations, and test get-induced recency plus eviction.
   - **Unlocks:** DesignRedis, Order Book indexing, caches in URL Shortener, and confidence with compound data structures.

3. **[Splitwise](../splitwise/README.md)** — value objects, strategies, and ledger thinking

   - **Why here:** It builds richer modelling on top of basic OOP while keeping infrastructure out of the problem.
   - **Learn:** money value objects, exact/equal/percentage split strategies, validation before mutation, balance ledger, settlement, and testing unordered domain results.
   - **Completion gate:** Add an expense through two split strategies, reject an invalid split without corrupting balances, and explain the ledger invariant.
   - **Unlocks:** Fraud rules, Risk rules, payment/booking modelling, and strategy selection without pattern overuse.

4. **[Elevator System](../elevator-system/README.md)** — state machines and scheduling

   - **Why here:** It introduces continuously changing state and policy decisions after you understand static object relationships.
   - **Learn:** elevator-car state, direction and stop queues, dispatch Strategy, capacity constraints, snapshots, and separating movement mechanics from scheduling policy.
   - **Completion gate:** Trace an external request through dispatch and movement, explain invalid/full-car behavior, and swap scheduling policy without changing the car.
   - **Unlocks:** OMS lifecycle states, FIX session state, queue ownership, and matching-engine event loops.

5. **[Token Bucket Rate Limiter](../token-bucket-rate-limiter/README.md)** — time and concurrency

   - **Why here:** It is a small design with very high interview ROI that forces precise reasoning about elapsed time, atomicity, and testability.
   - **Learn:** lazy refill math, capacity versus refill rate, injected `TimeSource`, per-key buckets, lock granularity, retry-after calculation, and deterministic clock-based tests.
   - **Completion gate:** Derive the refill formula, test without sleeping, explain two concurrent acquisitions, and state the local-versus-distributed trade-off.
   - **Unlocks:** Exchange Gateway throttling, TTL reasoning in DesignRedis, BookMyShow locking, and general concurrency discussions.

6. **[BookMyShow](../book-my-show/README.md)** — reservation consistency

   - **Why here:** It combines the modelling, state-machine, Strategy, and concurrency lessons from the first five projects in a familiar interview domain.
   - **Learn:** per-show inventory ownership, seat locking, hold/confirm/fail lifecycle, payment adapter, atomic multi-seat reservation, and race-condition prevention.
   - **Completion gate:** Prove that two users cannot confirm the same seat, walk through payment failure, and identify the transaction/lock boundary.
   - **Unlocks:** OMS race handling, order reservation, idempotency follow-ups, and consistency discussions.

7. **[Fraud Pattern Detection](../fraud-pattern-detection/README.md)** — explainable rule pipelines

   - **Why here:** It reinforces Strategy and composition before the financially specialized Pre-Trade Risk Engine.
   - **Learn:** ordered rules, facts/context, scoring, APPROVE/REVIEW/BLOCK policy, state-store boundary, explainable reasons, and adding a rule without changing orchestration.
   - **Completion gate:** Add one independent rule, show a decision with reasons, and explain rule ordering, short-circuiting, and state freshness.
   - **Unlocks:** Pre-Trade Risk, policy engines, validation pipelines, and auditable decisions.

8. **[DesignRedis](../DesignRedis/README.md)** — integrated in-memory data store

   - **Why here:** It deliberately combines previously learned maps, LRU, clocks, types, commands, and synchronization into a broader component.
   - **Learn:** typed values, TTL/lazy expiry, capacity eviction, command parsing, synchronized compound mutations, REPL boundary, and the difference between applied LRU and the canonical LRU algorithm.
   - **Completion gate:** Trace SET/GET/TTL and one collection command, explain expiry-versus-eviction, and identify why the standalone LRU project remains the canonical cache answer.
   - **Unlocks:** URL Shortener caching, persistence boundaries, and larger component decomposition.

9. **[URL Shortener](../url-shortener/README.md)** — layered service LLD

   - **Why here:** It is the bridge from pure in-memory LLD into APIs, database source-of-truth, cache-aside, collision handling, and application layering.
   - **Learn:** controller/service/repository boundaries, Base62 identifiers, uniqueness and collision retry, MySQL source of truth, Redis cache-aside, redirects, and failure behavior.
   - **Completion gate:** Explain create and redirect flows, cache miss/stale-cache behavior, unique-code generation, and what must remain transactional.
   - **Unlocks:** production-shaped backend design and the ability to keep infrastructure outside the domain core.

10. **[Order Management System](../order-management-system/README.md)** — explicit trading lifecycle

   - **Why here:** It applies state-machine and consistency skills to the central trading-side aggregate before introducing protocol details.
   - **Learn:** pending/new/partial/filled/cancelled/rejected states, acknowledgements, fills racing cancel/replace, invalid transitions, immutable snapshots, and append-only audit events.
   - **Completion gate:** Draw the state machine from memory, process partial fill then cancel, handle a fill racing a pending request, and reject an impossible transition.
   - **Unlocks:** Exchange Gateway event handling, FIX execution reports, reconciliation, and end-to-end order flow.

11. **[Pre-Trade Risk Engine](../pre-trade-risk-engine/README.md)** — trading rule composition

   - **Why here:** Fraud rules provide the Strategy foundation; OMS provides the order context. This project adds financial arithmetic and exposure reasoning.
   - **Learn:** quantity, notional, signed exposure, price-deviation basis points, kill switch, immutable risk context, collected violations, and the atomic-reservation follow-up.
   - **Completion gate:** Evaluate an accepted and multi-rule rejected order, explain exposure-reducing trades, handle arithmetic units safely, and identify the concurrent stale-exposure race.
   - **Unlocks:** Order acceptance flow, trading risk systems, and production exposure reservation.

12. **[DesignOrderBook](../DesignOrderBook/README.md)** — venue-side market state

   - **Why here:** It demands compound data structures, deterministic ordering, lifecycle reasoning, and precise invariants; the earlier projects supply all four foundations.
   - **Learn:** bid/ask `TreeMap`s, FIFO price levels, active-order index, add/cancel/cancel-replace, partial fill, best bid/ask, integer prices, and lost priority on replacement.
   - **Completion gate:** Draw both sides of the book, execute add/cancel/replace, explain FIFO within price, state operation complexity, and identify the `O(1)` cancel-handle improvement.
   - **Unlocks:** Matching Engine, ExchangeLite, market microstructure, and deterministic event processing.

13. **[Matching Engine](../matching-engine/README.md)** — price-time execution algorithm

   - **Why here:** Matching is easier to understand after the Order Book's ownership and data structures are already automatic.
   - **Learn:** maker versus taker, market/limit crossing, best-price then FIFO priority, maker-price trades, partial fills across levels, non-resting market remainder, and single-writer determinism.
   - **Completion gate:** Hand-simulate a multi-level partial match, explain why trades use maker price, state what rests, and separate matching concerns from order-book storage concerns.
   - **Unlocks:** exchange engines, execution reports, journaling/replay, and venue-side system design.

14. **[FIX Session Manager](../fix-session-manager/README.md)** — ordered protocol state and recovery

   - **Why here:** Sequence recovery is specialized and mentally expensive; learn it only after ordinary state machines, event ordering, and trading lifecycle are comfortable.
   - **Learn:** logon, heartbeat, inbound/outbound sequence numbers, gaps, resend requests, `PossDup`, replay store, duplicate suppression, disconnect, and reconnect continuity.
   - **Completion gate:** Walk through expected, low, and high sequence numbers; replay an outbound range; and explain what state must survive reconnect.
   - **Unlocks:** Exchange Gateway reliability, real FIX engines, and distributed ordered-stream reasoning.

15. **[Exchange Gateway](../exchange-gateway/README.md)** — outbound venue boundary

   - **Why here:** It intentionally composes OMS events, FIX/session concepts, Adapter, throttling, queues, and reconnect behavior.
   - **Learn:** internal order model, FIX/OUCH-like protocol adapters, transport port, token-bucket backpressure, acknowledgement/execution translation, disconnected queue, reconnect flush, and sent-but-unacknowledged follow-up.
   - **Completion gate:** Route one order through both adapters, throttle and later flush another, decode a fill, and explain how production reconciliation prevents unsafe duplicate sends.
   - **Unlocks:** exchange-connectivity system design and end-to-end electronic trading architecture.

16. **[FIX Gateway](../fix-gateway/README.md)** — final integration revision

   - **Why last:** This older composite intentionally overlaps parsing, basic session sequencing, small risk checks, mapping, and routing. Its ROI is highest as consolidation after the focused canonical projects, not as the first source for any one problem.
   - **Learn:** FIX body length/checksum, raw tag parsing/serialization, FIX-to-domain mapping, basic session validation, risk-and-routing orchestration, and execution-report conversion.
   - **Completion gate:** Trace raw NewOrderSingle to execution report, identify which responsibilities now belong to the focused Risk/Session/Gateway modules, and explain why the composite remains useful without becoming the canonical answer.
   - **Unlocks:** A complete review of the trading LLD chain and readiness to move into `SystemDesignProjects` for integrated failure, durability, scale, and operations.

Completing rank 16 means every runnable Maven project in this repository has been covered exactly once. Do not add another parallel ranking. Update this file when a new runnable LLD project is added so it remains exhaustive.
