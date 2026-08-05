package com.example.parkinglot.strategy;

import com.example.parkinglot.domain.ParkingLot;
import com.example.parkinglot.domain.ParkingSpot;
import com.example.parkinglot.domain.SpotType;
import com.example.parkinglot.domain.Vehicle;
import com.example.parkinglot.domain.VehicleType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class NearestAvailableSpotStrategy implements SpotAllocationStrategy {
    private static final Map<VehicleType, List<SpotType>> SPOT_PREFERENCE = Map.of(
            VehicleType.MOTORCYCLE, List.of(SpotType.MOTORCYCLE, SpotType.COMPACT, SpotType.LARGE),
            VehicleType.CAR, List.of(SpotType.COMPACT, SpotType.LARGE),
            VehicleType.ELECTRIC_CAR, List.of(SpotType.EV, SpotType.COMPACT, SpotType.LARGE),
            VehicleType.TRUCK, List.of(SpotType.LARGE)
    );

    @Override
    public Optional<ParkingSpot> findSpot(ParkingLot parkingLot, Vehicle vehicle) {
        Objects.requireNonNull(parkingLot, "parkingLot must not be null");
        Objects.requireNonNull(vehicle, "vehicle must not be null");

        Comparator<ParkingSpot> nearestCompatibleSpot = Comparator
                .comparingInt(ParkingSpot::floorNumber)
                .thenComparingInt(spot -> preferenceIndex(vehicle.type(), spot.type()))
                .thenComparing(ParkingSpot::spotId);

        return parkingLot.allSpots().stream()
                .filter(spot -> spot.canFit(vehicle))
                .min(nearestCompatibleSpot);
    }

    private static int preferenceIndex(VehicleType vehicleType, SpotType spotType) {
        List<SpotType> preferences = SPOT_PREFERENCE.get(vehicleType);
        int index = preferences.indexOf(spotType);
        return index == -1 ? Integer.MAX_VALUE : index;
    }
}

