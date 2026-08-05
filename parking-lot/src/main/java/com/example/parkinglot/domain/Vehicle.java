package com.example.parkinglot.domain;

import java.util.Locale;
import java.util.Objects;

public record Vehicle(String licensePlate, VehicleType type) {
    public Vehicle {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("licensePlate must not be blank");
        }
        licensePlate = licensePlate.trim().toUpperCase(Locale.ROOT);
        type = Objects.requireNonNull(type, "type must not be null");
    }
}

