package com.example.elevator.domain;

import java.util.List;

public record ElevatorSnapshot(
	String id,
	int currentFloor,
	Direction direction,
	ElevatorStatus status,
	int passengerLoad,
	List<Integer> upwardStops,
	List<Integer> downwardStops
) {
}
