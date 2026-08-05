package com.example.elevator.domain;

public record HallCall(int floor, Direction direction) {

	public HallCall {
		if (direction == null || direction == Direction.NONE) {
			throw new IllegalArgumentException("Hall call direction must be UP or DOWN");
		}
	}
}
