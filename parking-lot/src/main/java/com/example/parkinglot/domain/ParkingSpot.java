package com.example.parkinglot.domain;

import com.example.parkinglot.exception.SpotUnavailableException;

import java.util.Objects;
import java.util.Optional;

public final class ParkingSpot {
    private final String spotId;
    private final int floorNumber;
    private final SpotType type;
    private SpotStatus status;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, int floorNumber, SpotType type) {
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("spotId must not be blank");
        }
        if (floorNumber < 1) {
            throw new IllegalArgumentException("floorNumber must be positive");
        }

        this.spotId = spotId.trim();
        this.floorNumber = floorNumber;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.status = SpotStatus.AVAILABLE;
    }

    public boolean canFit(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        return isAvailable() && type.canFit(vehicle.type());
    }

    public void occupy(Vehicle vehicle) {
        if (!canFit(vehicle)) {
            throw new SpotUnavailableException("spot " + spotId + " cannot fit " + vehicle.type());
        }
        this.parkedVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }

    public Vehicle release() {
        if (isAvailable()) {
            throw new SpotUnavailableException("spot " + spotId + " is already available");
        }

        Vehicle releasedVehicle = parkedVehicle;
        this.parkedVehicle = null;
        this.status = SpotStatus.AVAILABLE;
        return releasedVehicle;
    }

    public String spotId() {
        return spotId;
    }

    public int floorNumber() {
        return floorNumber;
    }

    public SpotType type() {
        return type;
    }

    public SpotStatus status() {
        return status;
    }

    public Optional<Vehicle> parkedVehicle() {
        return Optional.ofNullable(parkedVehicle);
    }

    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }
}

