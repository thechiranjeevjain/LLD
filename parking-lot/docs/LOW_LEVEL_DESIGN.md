# Low-Level Design: Parking Lot

## Interview Scope

Design a parking lot that supports multiple floors, typed parking spots, vehicle compatibility, ticketing, payment, pricing, and availability queries.

Functional requirements:

- Park motorcycles, cars, electric cars, and trucks.
- Allocate a compatible available spot.
- Prevent the same vehicle from holding two active tickets.
- Generate a ticket at entry.
- Calculate parking fee at exit.
- Capture payment and release the spot.
- Query availability by vehicle or spot type.

Non-functional requirements:

- Allocation, pricing, and payment should be replaceable.
- In-memory state transitions should be consistent.
- The service should expose a small workflow-focused API.

## Core Class Diagram

```mermaid
classDiagram
    class ParkingLotService {
        -ParkingLot parkingLot
        -SpotAllocationStrategy allocationStrategy
        -PricingStrategy pricingStrategy
        -PaymentProcessor paymentProcessor
        -Map~String, Ticket~ activeTicketsById
        +park(Vehicle) Ticket
        +unpark(String, PaymentMethod) ParkingReceipt
        +availableSpotCount(VehicleType) long
        +availableSpotsByType() Map~SpotType, Long~
    }

    class ParkingLot {
        -String name
        -List~ParkingFloor~ floors
        +withFloors(String, int, Map~SpotType, Integer~) ParkingLot
        +allSpots() List~ParkingSpot~
    }

    class ParkingFloor {
        -int floorNumber
        -List~ParkingSpot~ spots
        +of(int, Map~SpotType, Integer~) ParkingFloor
    }

    class ParkingSpot {
        -String spotId
        -int floorNumber
        -SpotType type
        -SpotStatus status
        +occupy() void
        +release() void
    }

    class SpotAllocationStrategy {
        <<interface>>
        +findSpot(ParkingLot, Vehicle) Optional~ParkingSpot~
    }

    class NearestAvailableSpotStrategy

    class PricingStrategy {
        <<interface>>
        +calculateFee(Ticket, Instant) BigDecimal
    }

    class HourlyPricingStrategy

    class PaymentProcessor {
        <<interface>>
        +charge(BigDecimal, PaymentMethod, Instant) Payment
    }

    class InMemoryPaymentProcessor
    class Ticket
    class Vehicle
    class Payment
    class ParkingReceipt

    ParkingLotService --> ParkingLot
    ParkingLotService --> SpotAllocationStrategy
    ParkingLotService --> PricingStrategy
    ParkingLotService --> PaymentProcessor
    SpotAllocationStrategy <|.. NearestAvailableSpotStrategy
    PricingStrategy <|.. HourlyPricingStrategy
    PaymentProcessor <|.. InMemoryPaymentProcessor
    ParkingLot "1" *-- "many" ParkingFloor
    ParkingFloor "1" *-- "many" ParkingSpot
    Ticket --> Vehicle
    Ticket --> ParkingSpot
    Ticket --> Payment
    ParkingReceipt --> Ticket
```

## Main Responsibilities

| Component | Responsibility |
| --- | --- |
| `ParkingLotService` | Coordinates park, unpark, ticket lookup, availability, pricing, and payment. |
| `ParkingLot` | Aggregate of floors and all spots. |
| `ParkingFloor` | Groups spots on one floor and builds spot ids. |
| `ParkingSpot` | Owns spot type and occupied/free state. |
| `Ticket` | Tracks vehicle, assigned spot, entry time, exit time, fee, payment, and status. |
| `SpotAllocationStrategy` | Selects a compatible spot. |
| `PricingStrategy` | Computes fee from ticket and exit time. |
| `PaymentProcessor` | Captures payment outside core parking rules. |

## Park Flow

```mermaid
sequenceDiagram
    participant V as Vehicle
    participant S as ParkingLotService
    participant A as SpotAllocationStrategy
    participant P as ParkingSpot

    V->>S: park(vehicle)
    S->>S: check duplicate active vehicle
    S->>A: findSpot(parkingLot, vehicle)
    A-->>S: compatible available spot
    S->>P: occupy()
    S->>S: create active Ticket
    S-->>V: Ticket
```

## Unpark Flow

```mermaid
sequenceDiagram
    participant U as User
    participant S as ParkingLotService
    participant Price as PricingStrategy
    participant Pay as PaymentProcessor
    participant Spot as ParkingSpot

    U->>S: unpark(ticketId, method)
    S->>S: find active ticket
    S->>Price: calculateFee(ticket, exitTime)
    Price-->>S: fee
    S->>Pay: charge(fee, method, exitTime)
    Pay-->>S: payment
    S->>Spot: release()
    S->>S: close ticket and move to history
    S-->>U: ParkingReceipt
```

## Design Patterns

| Pattern | Where | Why it matters in interview discussion |
| --- | --- | --- |
| Strategy | `SpotAllocationStrategy`, `PricingStrategy` | Allocation and pricing policies can evolve independently from parking workflow. |
| Adapter / Port | `PaymentProcessor` | Keeps payment-provider details outside the domain service. |
| Facade / Application Service | `ParkingLotService` | Provides one workflow API and hides internal indexes. |
| Factory Method | `ParkingLot.withFloors`, `ParkingFloor.of` | Builds consistent test/demo layouts. |
| State Machine | `TicketStatus`, `SpotStatus` | Makes ticket and spot lifecycle transitions explicit. |

## Data Consistency

`ParkingLotService` synchronizes workflow methods so the following invariants hold:

- One active ticket per vehicle license plate.
- A spot can be assigned only when it is available.
- A ticket moves from active to closed exactly once.
- Releasing a spot and closing a ticket happen in the same service operation.
- Closed tickets remain queryable separately from active tickets.

## Extension Points

- Add reserved parking by creating a new `SpotAllocationStrategy`.
- Add slab, weekend, or subscription pricing by creating another `PricingStrategy`.
- Add real payment integrations behind `PaymentProcessor`.
- Add EV charging metadata to `ParkingSpot` or a separate charging service.
- Add persistence with repositories for parking lots, tickets, and payments.

## Interview Talking Points

- Vehicle compatibility belongs in `SpotType.canFit(VehicleType)` so allocation strategies do not duplicate fit rules.
- Synchronized service methods are clear for LLD; production systems would use database transactions and row-level locks.
- Allocation can be optimized with per-floor availability indexes when scans become expensive.
