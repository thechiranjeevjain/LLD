package com.example.parkinglot.exception;

public final class VehicleAlreadyParkedException extends ParkingLotException {
    public VehicleAlreadyParkedException(String message) {
        super(message);
    }
}

