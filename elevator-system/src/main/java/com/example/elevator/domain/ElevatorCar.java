package com.example.elevator.domain;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class ElevatorCar {

	private final String id;
	private final int minFloor;
	private final int maxFloor;
	private final int maxPassengerLoad;
	private final NavigableSet<Integer> upwardStops = new TreeSet<>();
	private final NavigableSet<Integer> downwardStops = new TreeSet<>(Comparator.reverseOrder());

	private int currentFloor;
	private int passengerLoad;
	private Direction direction = Direction.NONE;
	private ElevatorStatus status = ElevatorStatus.IDLE;

	public ElevatorCar(String id, int minFloor, int maxFloor, int startFloor, int maxPassengerLoad) {
		this.id = requireText(id, "id");
		if (minFloor > maxFloor) {
			throw new IllegalArgumentException("minFloor cannot be greater than maxFloor");
		}
		if (maxPassengerLoad <= 0) {
			throw new IllegalArgumentException("maxPassengerLoad must be positive");
		}
		this.minFloor = minFloor;
		this.maxFloor = maxFloor;
		this.maxPassengerLoad = maxPassengerLoad;
		validateFloor(startFloor);
		this.currentFloor = startFloor;
	}

	public synchronized String id() {
		return id;
	}

	public synchronized int currentFloor() {
		return currentFloor;
	}

	public synchronized Direction direction() {
		return direction;
	}

	public synchronized ElevatorStatus status() {
		return status;
	}

	public synchronized boolean canAcceptPickup() {
		return passengerLoad < maxPassengerLoad;
	}

	public synchronized boolean supportsFloor(int floor) {
		return floor >= minFloor && floor <= maxFloor;
	}

	public synchronized void boardPassengers(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Passenger count must be positive");
		}
		if (passengerLoad + count > maxPassengerLoad) {
			throw new IllegalStateException("Elevator " + id + " would exceed capacity");
		}
		passengerLoad += count;
	}

	public synchronized void leavePassengers(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Passenger count must be positive");
		}
		if (count > passengerLoad) {
			throw new IllegalStateException("Elevator " + id + " cannot unload more passengers than it has");
		}
		passengerLoad -= count;
	}

	public synchronized boolean isMovingToward(int floor, Direction requestedDirection) {
		validateFloor(floor);
		if (requestedDirection == null || requestedDirection == Direction.NONE || direction != requestedDirection) {
			return false;
		}
		return direction == Direction.UP ? floor >= currentFloor : floor <= currentFloor;
	}

	public synchronized int estimatePickupCost(int floor, Direction requestedDirection, int directionChangePenalty) {
		validateFloor(floor);
		if (!canAcceptPickup()) {
			return Integer.MAX_VALUE;
		}
		int distance = Math.abs(currentFloor - floor);
		if (status == ElevatorStatus.IDLE || direction == Direction.NONE || isMovingToward(floor, requestedDirection)) {
			return distance;
		}
		return directionChangePenalty + distance;
	}

	public synchronized void requestStop(int floor) {
		validateFloor(floor);
		if (floor == currentFloor) {
			removeStopAtCurrentFloor();
			status = ElevatorStatus.DOORS_OPEN;
			if (!hasPendingStops()) {
				direction = Direction.NONE;
			}
			return;
		}
		if (floor > currentFloor) {
			upwardStops.add(floor);
		}
		else {
			downwardStops.add(floor);
		}
		if (status == ElevatorStatus.IDLE || direction == Direction.NONE) {
			chooseDirection();
			status = ElevatorStatus.MOVING;
		}
	}

	public synchronized ElevatorSnapshot step() {
		if (status == ElevatorStatus.DOORS_OPEN) {
			closeDoors();
			return snapshot();
		}
		if (!hasPendingStops()) {
			status = ElevatorStatus.IDLE;
			direction = Direction.NONE;
			return snapshot();
		}
		if (direction == Direction.NONE) {
			chooseDirection();
		}
		status = ElevatorStatus.MOVING;
		moveOneFloor();
		if (removeStopAtCurrentFloor()) {
			status = ElevatorStatus.DOORS_OPEN;
			if (!hasPendingStops()) {
				direction = Direction.NONE;
			}
		}
		else if (!hasStopsInCurrentDirection()) {
			chooseDirection();
		}
		return snapshot();
	}

	public synchronized ElevatorSnapshot snapshot() {
		return new ElevatorSnapshot(
			id,
			currentFloor,
			direction,
			status,
			passengerLoad,
			List.copyOf(upwardStops),
			List.copyOf(downwardStops)
		);
	}

	private void closeDoors() {
		if (!hasPendingStops()) {
			status = ElevatorStatus.IDLE;
			direction = Direction.NONE;
			return;
		}
		chooseDirection();
		status = ElevatorStatus.MOVING;
	}

	private void moveOneFloor() {
		if (direction == Direction.UP) {
			currentFloor += 1;
		}
		else if (direction == Direction.DOWN) {
			currentFloor -= 1;
		}
	}

	private boolean removeStopAtCurrentFloor() {
		return upwardStops.remove(currentFloor) | downwardStops.remove(currentFloor);
	}

	private boolean hasPendingStops() {
		return !upwardStops.isEmpty() || !downwardStops.isEmpty();
	}

	private boolean hasStopsInCurrentDirection() {
		return switch (direction) {
			case UP -> !upwardStops.isEmpty();
			case DOWN -> !downwardStops.isEmpty();
			case NONE -> false;
		};
	}

	private void chooseDirection() {
		if (direction == Direction.UP && !upwardStops.isEmpty()) {
			return;
		}
		if (direction == Direction.DOWN && !downwardStops.isEmpty()) {
			return;
		}
		if (upwardStops.isEmpty() && downwardStops.isEmpty()) {
			direction = Direction.NONE;
			return;
		}
		if (upwardStops.isEmpty()) {
			direction = Direction.DOWN;
			return;
		}
		if (downwardStops.isEmpty()) {
			direction = Direction.UP;
			return;
		}
		int nearestUpDistance = Math.abs(upwardStops.first() - currentFloor);
		int nearestDownDistance = Math.abs(downwardStops.first() - currentFloor);
		direction = nearestUpDistance <= nearestDownDistance ? Direction.UP : Direction.DOWN;
	}

	private void validateFloor(int floor) {
		if (!supportsFloor(floor)) {
			throw new IllegalArgumentException("Floor " + floor + " is outside elevator " + id + " service range");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
