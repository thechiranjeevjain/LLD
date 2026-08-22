# Elevator System

> **40–60 minute boundary — focused slice:** Implement car state, controller, one selection strategy, and one movement step. Discuss advanced scheduling and operational modes rather than coding them. See the [strict code-scope guide](../docs/40_60_MINUTE_CODE_SCOPE.md).

Low-level design for an elevator dispatch system. The implementation models a bank of elevator cars, hall calls, in-car destination requests, a pluggable assignment strategy, and deterministic step-by-step movement for testing.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, dispatch sequence, design patterns, movement state rules, and extension points.

## Run Tests

```powershell
mvn test
```

## Run Demo

```powershell
mvn exec:java
```

## Requirements

| Requirement | Design |
| --- | --- |
| Multiple elevators | `ElevatorController` owns a stable collection of `ElevatorCar` objects. |
| Hall calls | `requestPickup(floor, direction)` assigns the call to one car through an `ElevatorSelectionStrategy`. |
| In-car requests | `requestDropOff(elevatorId, destinationFloor)` adds a stop to the selected car. |
| Direction-aware movement | Each car keeps upward and downward stop queues, then continues in the current direction until that queue is exhausted. |
| Capacity handling | Full cars are ignored by the default pickup strategy. |
| Extensible scheduling | `ElevatorSelectionStrategy` can be replaced without changing car movement logic. |
| Testable simulation | `step()` advances each car by one tick, making dispatch and stop behavior deterministic. |

## API Design

| Method | Purpose |
| --- | --- |
| `requestPickup(HallCall call)` | Assigns an elevator for an outside floor request and returns the selected car id. |
| `requestDropOff(String elevatorId, int floor)` | Adds a destination selected inside an elevator. |
| `step()` | Advances every elevator by one simulation tick and returns snapshots. |
| `snapshots()` | Returns immutable current state for all cars. |

## Main Classes

| Class | Responsibility |
| --- | --- |
| `ElevatorController` | Public facade for pickup, drop-off, simulation, and status reads. |
| `ElevatorCar` | Owns current floor, direction, door state, capacity, and pending stop queues. |
| `ElevatorSelectionStrategy` | Strategy interface for assigning hall calls. |
| `NearestCarSelectionStrategy` | Picks the available car with the lowest pickup cost. |
| `HallCall` | Immutable outside request containing floor and desired direction. |
| `ElevatorSnapshot` | Immutable state view used by tests and clients. |

## Dispatch Strategy

The default strategy scores each car:

1. Full cars are not eligible.
2. Idle cars use absolute distance to the pickup floor.
3. Cars already moving in the requested direction and not yet past the caller use absolute distance.
4. Cars moving away are still eligible, but receive a direction-change penalty.

This keeps the scheduling policy simple and replaceable. More advanced strategies can estimate queued stop completion time, zoning, express elevators, maintenance mode, or traffic patterns without modifying `ElevatorCar`.

## Movement Rules

Each `step()` performs one state transition:

1. If doors are open, close them and choose the next direction if stops remain.
2. If idle with no stops, stay idle.
3. Move one floor in the current direction.
4. If the current floor is queued, open doors and remove that stop.
5. When one direction has no more queued stops, reverse only if the opposite queue has work.

## Extensions

| Feature | Where to extend |
| --- | --- |
| Maintenance mode | Add availability state to `ElevatorCar` and filter it in the strategy. |
| Emergency stop | Add a high-priority stop queue or override state. |
| Destination dispatch | Replace hall calls with passenger groups containing destination floors. |
| Door dwell timing | Replace the single door-close tick with a configurable countdown. |
| Traffic optimization | Implement a strategy using ETA from queued stops instead of simple distance. |
