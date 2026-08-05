package com.example.parkinglot.domain;

public enum SpotType {
    MOTORCYCLE,
    COMPACT,
    LARGE,
    EV;

    public boolean canFit(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> this == MOTORCYCLE || this == COMPACT || this == LARGE;
            case CAR -> this == COMPACT || this == LARGE;
            case ELECTRIC_CAR -> this == EV || this == COMPACT || this == LARGE;
            case TRUCK -> this == LARGE;
        };
    }
}

