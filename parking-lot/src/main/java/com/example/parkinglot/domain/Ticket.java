package com.example.parkinglot.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final Instant entryTime;
    private TicketStatus status;
    private Instant exitTime;
    private BigDecimal fee;
    private Payment payment;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSpot parkingSpot, Instant entryTime) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }

        this.ticketId = ticketId.trim();
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        this.parkingSpot = Objects.requireNonNull(parkingSpot, "parkingSpot must not be null");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        this.status = TicketStatus.ACTIVE;
    }

    public void close(Instant exitTime, BigDecimal fee, Payment payment) {
        if (status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("ticket is already closed");
        }
        Objects.requireNonNull(exitTime, "exitTime must not be null");
        Objects.requireNonNull(fee, "fee must not be null");
        Objects.requireNonNull(payment, "payment must not be null");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("exitTime must not be before entryTime");
        }

        this.exitTime = exitTime;
        this.fee = fee;
        this.payment = payment;
        this.status = TicketStatus.PAID;
    }

    public String ticketId() {
        return ticketId;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public ParkingSpot parkingSpot() {
        return parkingSpot;
    }

    public Instant entryTime() {
        return entryTime;
    }

    public TicketStatus status() {
        return status;
    }

    public Optional<Instant> exitTime() {
        return Optional.ofNullable(exitTime);
    }

    public Optional<BigDecimal> fee() {
        return Optional.ofNullable(fee);
    }

    public Optional<Payment> payment() {
        return Optional.ofNullable(payment);
    }
}

