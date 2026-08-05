package com.example.elevator.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.elevator.domain.Direction;
import com.example.elevator.domain.ElevatorCar;
import com.example.elevator.domain.ElevatorSnapshot;
import com.example.elevator.domain.HallCall;

public final class ElevatorController {

	private final Map<String, ElevatorCar> carsById;
	private final ElevatorSelectionStrategy selectionStrategy;

	public ElevatorController(Collection<ElevatorCar> cars, ElevatorSelectionStrategy selectionStrategy) {
		Objects.requireNonNull(cars, "cars must not be null");
		this.selectionStrategy = Objects.requireNonNull(selectionStrategy, "selectionStrategy must not be null");
		if (cars.isEmpty()) {
			throw new IllegalArgumentException("At least one elevator is required");
		}
		this.carsById = new LinkedHashMap<>();
		for (ElevatorCar car : cars) {
			ElevatorCar previous = carsById.put(car.id(), car);
			if (previous != null) {
				throw new IllegalArgumentException("Duplicate elevator id: " + car.id());
			}
		}
	}

	public synchronized String requestPickup(int floor, Direction direction) {
		return requestPickup(new HallCall(floor, direction));
	}

	public synchronized String requestPickup(HallCall call) {
		Objects.requireNonNull(call, "call must not be null");
		if (carsById.values().stream().noneMatch(car -> car.supportsFloor(call.floor()))) {
			throw new IllegalArgumentException("No elevator services floor " + call.floor());
		}
		ElevatorCar selectedCar = selectionStrategy
			.selectCar(carsById.values(), call.floor(), call.direction())
			.orElseThrow(() -> new IllegalStateException("No available elevator can accept pickup at floor " + call.floor()));
		selectedCar.requestStop(call.floor());
		return selectedCar.id();
	}

	public synchronized void requestDropOff(String elevatorId, int destinationFloor) {
		ElevatorCar car = carsById.get(elevatorId);
		if (car == null) {
			throw new IllegalArgumentException("Unknown elevator id: " + elevatorId);
		}
		car.requestStop(destinationFloor);
	}

	public synchronized List<ElevatorSnapshot> step() {
		return carsById.values().stream()
			.map(ElevatorCar::step)
			.toList();
	}

	public synchronized List<ElevatorSnapshot> snapshots() {
		return carsById.values().stream()
			.map(ElevatorCar::snapshot)
			.toList();
	}
}
