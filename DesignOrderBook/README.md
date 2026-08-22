# Design Order Book

> **Canonical scope and 40–60 minute boundary — focused slice:** Use this project for **Order Book (LLD #19)**, but implement only the order, price-level maps/queues, add/match-or-fill, cancel, and top-of-book slice defined in the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md). Its broader matching/snapshot code is supporting material; for **Matching Engine (LLD #20)** use [matching-engine](../matching-engine/README.md). See the [trading-project relationship guide](../docs/TRADING_PROJECT_RELATIONSHIP_GUIDE.md) for system boundaries.

Java 17 low-level design project for an in-memory exchange order book.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, matching flow, design patterns, invariants, and production tradeoffs.

The project models a single-process matching engine with one `OrderBook` per symbol. It supports:

- Limit buy/sell orders
- Market buy/sell orders
- GTC and IOC time-in-force
- Price-time priority
- Partial fills
- Order cancellation
- Atomic cancel-replace with a new order ID and lost time priority
- Top-of-book and depth snapshots
- Execution reports and trades

## Run

```bash
mvn test
mvn exec:java
```

## Design Entry Points

- `org.chijai.orderbook.engine.MatchingEngine`: routes requests to per-symbol books.
- `org.chijai.orderbook.engine.OrderBook`: owns price levels, active orders, matching, cancellation, and snapshots.
- `org.chijai.orderbook.model.OrderRequest`: immutable input command.
- `org.chijai.orderbook.model.ExecutionReport`: result of placing or cancelling an order.
- `org.chijai.orderbook.model.Trade`: immutable trade event emitted by matching.

## Matching Rules

1. Best price wins.
2. FIFO wins inside the same price level.
3. Trades execute at the resting order price.
4. Market orders never rest on the book.
5. IOC orders expire any unfilled quantity.
6. GTC limit orders rest any unfilled quantity.
7. Order IDs are accepted once and cannot be reused.
8. Replace validates first, then atomically cancels the old order and adds a new order at the back of its price level.

## Complexity

- Add resting order: `O(log P)` where `P` is number of price levels.
- Match order: `O(L + T)`, where `L` is crossed price levels and `T` is filled resting orders.
- Cancel active order: `O(Q)` within the price-level queue in this implementation.
- Snapshot depth `D`: `O(D + orders in returned levels)`.

For production cancellation, store a direct pointer/node handle per order to make removal `O(1)`.

## Project Structure

```text
src/main/java/org/chijai/orderbook
  OrderBookDemo.java
  engine/
    MatchingEngine.java
    OrderBook.java
  model/
    ExecutionReport.java
    Order.java
    OrderBookSnapshot.java
    OrderRequest.java
    OrderStatus.java
    OrderType.java
    PriceLevel.java
    Side.java
    TimeInForce.java
    Trade.java

src/test/java/org/chijai/orderbook/engine
  OrderBookTest.java
```

## Scope

This is intentionally in-memory and deterministic for LLD practice. It does not include persistence, networking, authentication, risk checks, market data fanout, or horizontal scaling.
