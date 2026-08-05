# Low-Level Design: Elevator System

## Interview Scope

Design an elevator dispatch system for a building with multiple elevator cars, hall calls, in-car destination requests, deterministic simulation ticks, and replaceable scheduling logic.

Functional requirements:

- Manage multiple elevator cars across a configured floor range.
- Accept hall calls with pickup floor and desired direction.
- Accept in-car drop-off requests.
- Move cars one step at a time for deterministic tests.
- Open and close doors at requested stops.
- Avoid assigning pickup calls to cars that cannot accept passengers.

Non-functional requirements:

- Scheduling policy must be replaceable.
- Car movement rules should remain independent from dispatch decisions.
- State snapshots should be immutable.
- Operations should be safe for concurrent callers in the in-memory implementation.

## Core Class Diagram

```mermaid
classDiagram
    class ElevatorController {
        -Map~String, ElevatorCar~ carsById
        -ElevatorSelectionStrategy selectionStrategy
        +requestPickup(int, Direction) String
        +requestPickup(HallCall) String
        +requestDropOff(String, int) void
        +step() List~ElevatorSnapshot~
        +snapshots() List~ElevatorSnapshot~
    }

    class ElevatorCar {
        -String id
        -int currentFloor
        -Direction direction
        -ElevatorStatus status
        -NavigableSet~Integer~ upwardStops
        -NavigableSet~Integer~ downwardStops
        +requestStop(int) void
        +step() ElevatorSnapshot
        +estimatePickupCost(int, Direction, int) int
        +isMovingToward(int, Direction) boolean
    }

    class ElevatorSelectionStrategy {
        <<interface>>
        +selectElevator(HallCall, Collection~ElevatorCar~) ElevatorCar
    }

    class NearestCarSelectionStrategy
    class HallCall
    class ElevatorSnapshot
    class Direction
    class ElevatorStatus

    ElevatorController --> ElevatorCar
    ElevatorController --> ElevatorSelectionStrategy
    ElevatorSelectionStrategy <|.. NearestCarSelectionStrategy
    NearestCarSelectionStrategy --> ElevatorCar
    ElevatorCar --> ElevatorSnapshot
    ElevatorCar --> Direction
    ElevatorCar --> ElevatorStatus
    ElevatorController --> HallCall
```

## Main Responsibilities

| Component | Responsibility |
| --- | --- |
| `ElevatorController` | Facade for pickup requests, drop-off requests, simulation, and snapshots. |
| `ElevatorCar` | Owns car-local state: floor, direction, status, capacity, and pending stops. |
| `ElevatorSelectionStrategy` | Assigns hall calls to a car. |
| `NearestCarSelectionStrategy` | Scores eligible cars by pickup cost and selects the cheapest. |
| `HallCall` | Immutable pickup request. |
| `ElevatorSnapshot` | Immutable state view for callers and tests. |

## Dispatch Flow

```mermaid
sequenceDiagram
    participant U as User
    participant C as ElevatorController
    participant S as ElevatorSelectionStrategy
    participant E as ElevatorCar

    U->>C: requestPickup(floor, direction)
    C->>S: selectElevator(call, cars)
    S->>E: estimatePickupCost(floor, direction, penalty)
    S-->>C: selected car
    C->>E: requestStop(floor)
    C-->>U: elevator id
```

## Movement Flow

```mermaid
flowchart TD
    A["step()"] --> B{"Doors open?"}
    B -- "Yes" --> C["Close doors and choose next direction"]
    B -- "No" --> D{"Any pending stops?"}
    D -- "No" --> E["Stay IDLE"]
    D -- "Yes" --> F["Move one floor in current direction"]
    F --> G{"Current floor is a queued stop?"}
    G -- "Yes" --> H["Remove stop and open doors"]
    G -- "No" --> I["Keep moving"]
    H --> J["Return snapshot"]
    I --> J
    C --> J
    E --> J
```

## Data Structures

| Need | Structure | Reason |
| --- | --- | --- |
| Stable car registry | `LinkedHashMap<String, ElevatorCar>` | Deterministic iteration and snapshots. |
| Upward stops | `TreeSet<Integer>` ascending | Visit higher floors in increasing order. |
| Downward stops | `TreeSet<Integer>` descending | Visit lower floors in decreasing order. |
| State view | `ElevatorSnapshot` record | Prevent callers from mutating car internals. |

## Design Patterns

| Pattern | Where | Why it matters in interview discussion |
| --- | --- | --- |
| Strategy | `ElevatorSelectionStrategy` | Dispatch policy can change without changing car state transitions. |
| Facade | `ElevatorController` | Provides one API for callers and hides the car registry. |
| State | `ElevatorStatus`, `Direction`, car transition methods | Makes movement and door lifecycle explicit. |
| Snapshot / DTO | `ElevatorSnapshot` | Exposes read-only state safely. |
| Encapsulation | `ElevatorCar` owns stop queues | Prevents outside code from corrupting movement order. |

## Consistency and Concurrency

`ElevatorController` synchronizes operations that assign calls, add drop-offs, and advance the simulation. `ElevatorCar` also synchronizes car-local operations. This keeps the in-memory demo safe while avoiding exposed mutable collections.

Important invariants:

- A car never accepts a stop outside its floor range.
- A pickup is assigned only to an eligible car.
- Direction reverses only when the current direction has no remaining stops.
- Snapshots return copies of pending stops.

## Extension Points

- Replace `NearestCarSelectionStrategy` with ETA-based, zoning, or destination-dispatch strategies.
- Add maintenance mode by adding car availability and filtering in the strategy.
- Add door dwell time by replacing the single open-door tick with a countdown.
- Add emergency override with a high-priority state and stop queue.
- Add capacity-aware boarding by modeling passengers and destinations.

## Interview Talking Points

- Keep scheduling and movement separate; otherwise every new dispatch strategy risks breaking car mechanics.
- `TreeSet` is enough for basic SCAN-style movement, but ETA-based systems need richer queue simulation.
- A production controller would likely shard by building or elevator bank and persist commands/events.
