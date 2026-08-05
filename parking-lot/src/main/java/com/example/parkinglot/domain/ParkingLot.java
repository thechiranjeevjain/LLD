package com.example.parkinglot.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ParkingLot {
    private final String name;
    private final List<ParkingFloor> floors;

    public ParkingLot(String name, List<ParkingFloor> floors) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (floors == null || floors.isEmpty()) {
            throw new IllegalArgumentException("parking lot must have at least one floor");
        }

        Set<Integer> floorNumbers = new HashSet<>();
        Set<String> spotIds = new HashSet<>();
        for (ParkingFloor floor : floors) {
            Objects.requireNonNull(floor, "floor must not be null");
            if (!floorNumbers.add(floor.floorNumber())) {
                throw new IllegalArgumentException("duplicate floor number: " + floor.floorNumber());
            }
            for (ParkingSpot spot : floor.spots()) {
                if (!spotIds.add(spot.spotId())) {
                    throw new IllegalArgumentException("duplicate spot id in lot: " + spot.spotId());
                }
            }
        }

        this.name = name.trim();
        this.floors = List.copyOf(floors);
    }

    public static ParkingLot withFloors(String name, int floorCount, Map<SpotType, Integer> spotCountsPerFloor) {
        if (floorCount < 1) {
            throw new IllegalArgumentException("floorCount must be positive");
        }

        List<ParkingFloor> floors = new ArrayList<>();
        for (int floorNumber = 1; floorNumber <= floorCount; floorNumber++) {
            floors.add(ParkingFloor.of(floorNumber, spotCountsPerFloor));
        }
        return new ParkingLot(name, floors);
    }

    public String name() {
        return name;
    }

    public List<ParkingFloor> floors() {
        return floors;
    }

    public List<ParkingSpot> allSpots() {
        return floors.stream()
                .flatMap(floor -> floor.spots().stream())
                .toList();
    }
}

