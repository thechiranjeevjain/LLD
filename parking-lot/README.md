# Parking Lot LLD

Standalone Java 17 implementation of a parking-lot low-level design.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, park/unpark sequences, design patterns, invariants, and extension points.

## What is modeled

- Multiple parking floors with typed spots.
- Vehicle compatibility rules for motorcycle, car, electric car, and truck.
- Pluggable spot allocation strategy.
- Pluggable pricing strategy.
- Ticket lifecycle from active to paid.
- In-memory payment processing.
- Availability queries and active-ticket tracking.

## Design

Core flow:

1. `ParkingLotService.park(vehicle)` checks duplicate active vehicles.
2. `SpotAllocationStrategy` chooses the nearest compatible available spot.
3. The spot is occupied and an active `Ticket` is created.
4. `ParkingLotService.unpark(ticketId, method)` calculates the fee.
5. Payment is captured, the spot is released, and the ticket is closed.

Main extension points:

- `SpotAllocationStrategy`: replace nearest-first assignment with reserved, premium, or random allocation.
- `PricingStrategy`: replace hourly pricing with slab, weekend, subscription, or dynamic pricing.
- `PaymentProcessor`: replace the in-memory success processor with a real payment gateway.

## Run

```bash
mvn test
mvn exec:java
```
