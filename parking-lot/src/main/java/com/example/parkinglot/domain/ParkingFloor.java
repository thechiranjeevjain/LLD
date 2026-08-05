package com.example.parkinglot.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        if (floorNumber < 1) {
            throw new IllegalArgumentException("floorNumber must be positive");
        }
        if (spots == null || spots.isEmpty()) {
            throw new IllegalArgumentException("floor must have at least one spot");
        }

        Set<String> spotIds = new HashSet<>();
        for (ParkingSpot spot : spots) {
            Objects.requireNonNull(spot, "spot must not be null");
            if (spot.floorNumber() != floorNumber) {
                throw new IllegalArgumentException("spot floor number does not match floor");
            }
            if (!spotIds.add(spot.spotId())) {
                throw new IllegalArgumentException("duplicate spot id on floor: " + spot.spotId());
            }
        }

        this.floorNumber = floorNumber;
        this.spots = List.copyOf(spots);
    }

    public static ParkingFloor of(int floorNumber, Map<SpotType, Integer> spotCounts) {
        Objects.requireNonNull(spotCounts, "spotCounts must not be null");

        List<ParkingSpot> spots = new ArrayList<>();
        for (SpotType spotType : SpotType.values()) {
            int count = spotCounts.getOrDefault(spotType, 0);
            if (count < 0) {
                throw new IllegalArgumentException("spot count must not be negative");
            }
            for (int spotNumber = 1; spotNumber <= count; spotNumber++) {
                String spotId = "F%d-%s-%02d".formatted(floorNumber, spotType.name(), spotNumber);
                spots.add(new ParkingSpot(spotId, floorNumber, spotType));
            }
        }
        return new ParkingFloor(floorNumber, spots);
    }

    public int floorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> spots() {
        return spots;
    }
}

