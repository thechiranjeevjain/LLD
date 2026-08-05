package com.example.elevator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.example.elevator.domain.Direction;
import com.example.elevator.domain.ElevatorCar;
import com.example.elevator.domain.ElevatorStatus;
import org.junit.jupiter.api.Test;

class ElevatorControllerTest {

	@Test
	void assignsNearestIdleElevatorForPickup() {
		ElevatorCar first = car("A", 0);
		ElevatorCar second = car("B", 7);
		ElevatorController controller = controller(first, second);

		String assignedCar = controller.requestPickup(2, Direction.UP);

		assertThat(assignedCar).isEqualTo("A");
		assertThat(first.snapshot().upwardStops()).containsExactly(2);

		controller.step();
		controller.step();

		assertThat(first.snapshot().currentFloor()).isEqualTo(2);
		assertThat(first.snapshot().status()).isEqualTo(ElevatorStatus.DOORS_OPEN);
	}

	@Test
	void prefersCompatibleMovingElevatorOverFartherIdleElevator() {
		ElevatorCar movingUp = car("A", 1);
		ElevatorCar idleFarAway = car("B", 9);
		movingUp.requestStop(10);
		movingUp.step();
		ElevatorController controller = controller(movingUp, idleFarAway);

		String assignedCar = controller.requestPickup(4, Direction.UP);

		assertThat(assignedCar).isEqualTo("A");
		assertThat(movingUp.snapshot().upwardStops()).containsExactly(4, 10);
	}

	@Test
	void avoidsElevatorMovingOppositeToRequestedDirectionWhenBetterCarExists() {
		ElevatorCar movingUp = car("A", 2);
		ElevatorCar movingDown = car("B", 8);
		movingUp.requestStop(10);
		movingDown.requestStop(1);
		ElevatorController controller = controller(movingUp, movingDown);

		String assignedCar = controller.requestPickup(6, Direction.DOWN);

		assertThat(assignedCar).isEqualTo("B");
		assertThat(movingDown.snapshot().downwardStops()).containsExactly(6, 1);
	}

	@Test
	void addsDropOffRequestToRequestedElevator() {
		ElevatorCar car = car("A", 0);
		ElevatorController controller = controller(car);

		controller.requestDropOff("A", 3);

		assertThat(car.snapshot().upwardStops()).containsExactly(3);
		controller.step();
		controller.step();
		controller.step();
		assertThat(car.snapshot().status()).isEqualTo(ElevatorStatus.DOORS_OPEN);
		assertThat(car.snapshot().currentFloor()).isEqualTo(3);
	}

	@Test
	void rejectsPickupOutsideServicedFloors() {
		ElevatorController controller = controller(car("A", 0));

		assertThatThrownBy(() -> controller.requestPickup(99, Direction.UP))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No elevator services floor 99");
	}

	@Test
	void ignoresFullElevatorsForPickupSelection() {
		ElevatorCar full = car("A", 1);
		ElevatorCar available = car("B", 4);
		full.boardPassengers(8);
		ElevatorController controller = controller(full, available);

		String assignedCar = controller.requestPickup(2, Direction.UP);

		assertThat(assignedCar).isEqualTo("B");
		assertThat(available.snapshot().downwardStops()).containsExactly(2);
	}

	private static ElevatorController controller(ElevatorCar... cars) {
		return new ElevatorController(List.of(cars), new NearestCarSelectionStrategy());
	}

	private static ElevatorCar car(String id, int startFloor) {
		return new ElevatorCar(id, 0, 10, startFloor, 8);
	}
}
