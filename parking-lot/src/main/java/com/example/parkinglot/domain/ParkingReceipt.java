package com.example.parkinglot.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record ParkingReceipt(
        String ticketId,
        Vehicle vehicle,
        String spotId,
        int floorNumber,
        Instant entryTime,
        Instant exitTime,
        BigDecimal fee,
        Payment payment
) {
    public ParkingReceipt {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("spotId must not be blank");
        }
        if (floorNumber < 1) {
            throw new IllegalArgumentException("floorNumber must be positive");
        }
        Objects.requireNonNull(entryTime, "entryTime must not be null");
        Objects.requireNonNull(exitTime, "exitTime must not be null");
        Objects.requireNonNull(fee, "fee must not be null");
        Objects.requireNonNull(payment, "payment must not be null");
    }

    public static ParkingReceipt from(Ticket ticket, Payment payment) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(payment, "payment must not be null");

        return new ParkingReceipt(
                ticket.ticketId(),
                ticket.vehicle(),
                ticket.parkingSpot().spotId(),
                ticket.parkingSpot().floorNumber(),
                ticket.entryTime(),
                ticket.exitTime().orElseThrow(),
                ticket.fee().orElseThrow(),
                payment
        );
    }
}

