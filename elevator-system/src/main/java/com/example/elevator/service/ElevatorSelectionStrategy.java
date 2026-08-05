package com.example.elevator.service;

import java.util.Collection;
import java.util.Optional;

import com.example.elevator.domain.Direction;
import com.example.elevator.domain.ElevatorCar;

public interface ElevatorSelectionStrategy {

	Optional<ElevatorCar> selectCar(Collection<ElevatorCar> cars, int pickupFloor, Direction direction);
}
