package com.example.elevator;

import java.util.List;

import com.example.elevator.domain.Direction;
import com.example.elevator.domain.ElevatorCar;
import com.example.elevator.domain.ElevatorSnapshot;
import com.example.elevator.service.ElevatorController;
import com.example.elevator.service.NearestCarSelectionStrategy;

public final class ElevatorSystemDemo {

	private ElevatorSystemDemo() {
	}

	public static void main(String[] args) {
		ElevatorController controller = new ElevatorController(
			List.of(
				new ElevatorCar("E1", 0, 10, 0, 8),
				new ElevatorCar("E2", 0, 10, 6, 8),
				new ElevatorCar("E3", 0, 10, 10, 8)
			),
			new NearestCarSelectionStrategy()
		);

		String firstCar = controller.requestPickup(3, Direction.UP);
		controller.requestDropOff(firstCar, 8);
		System.out.printf("Pickup at floor 3 going UP assigned to %s, then drop off at floor 8.%n", firstCar);

		String secondCar = controller.requestPickup(7, Direction.DOWN);
		controller.requestDropOff(secondCar, 1);
		System.out.printf("Pickup at floor 7 going DOWN assigned to %s, then drop off at floor 1.%n", secondCar);

		for (int tick = 1; tick <= 10; tick++) {
			System.out.printf("tick=%d%n", tick);
			for (ElevatorSnapshot snapshot : controller.step()) {
				System.out.printf("  %s floor=%d direction=%s status=%s up=%s down=%s%n",
					snapshot.id(),
					snapshot.currentFloor(),
					snapshot.direction(),
					snapshot.status(),
					snapshot.upwardStops(),
					snapshot.downwardStops()
				);
			}
		}
	}
}
