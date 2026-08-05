package com.example.parkinglot.strategy;

import com.example.parkinglot.domain.ParkingLot;
import com.example.parkinglot.domain.ParkingSpot;
import com.example.parkinglot.domain.Vehicle;

import java.util.Optional;

public interface SpotAllocationStrategy {
    Optional<ParkingSpot> findSpot(ParkingLot parkingLot, Vehicle vehicle);
}

