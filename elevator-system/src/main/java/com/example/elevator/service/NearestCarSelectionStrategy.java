package com.example.elevator.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import com.example.elevator.domain.Direction;
import com.example.elevator.domain.ElevatorCar;

public final class NearestCarSelectionStrategy implements ElevatorSelectionStrategy {

	private static final int DIRECTION_CHANGE_PENALTY = 100;

	@Override
	public Optional<ElevatorCar> selectCar(Collection<ElevatorCar> cars, int pickupFloor, Direction direction) {
		return cars.stream()
			.filter(ElevatorCar::canAcceptPickup)
			.filter(car -> car.supportsFloor(pickupFloor))
			.min(Comparator
				.comparingInt((ElevatorCar car) -> car.estimatePickupCost(pickupFloor, direction, DIRECTION_CHANGE_PENALTY))
				.thenComparing(ElevatorCar::id));
	}
}
