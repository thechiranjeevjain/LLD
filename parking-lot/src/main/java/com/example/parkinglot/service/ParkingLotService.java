package com.example.parkinglot.service;

import com.example.parkinglot.domain.ParkingLot;
import com.example.parkinglot.domain.ParkingReceipt;
import com.example.parkinglot.domain.ParkingSpot;
import com.example.parkinglot.domain.Payment;
import com.example.parkinglot.domain.PaymentMethod;
import com.example.parkinglot.domain.SpotType;
import com.example.parkinglot.domain.Ticket;
import com.example.parkinglot.domain.Vehicle;
import com.example.parkinglot.domain.VehicleType;
import com.example.parkinglot.exception.InvalidTicketException;
import com.example.parkinglot.exception.ParkingLotFullException;
import com.example.parkinglot.exception.VehicleAlreadyParkedException;
import com.example.parkinglot.strategy.HourlyPricingStrategy;
import com.example.parkinglot.strategy.NearestAvailableSpotStrategy;
import com.example.parkinglot.strategy.PricingStrategy;
import com.example.parkinglot.strategy.SpotAllocationStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class ParkingLotService {
    private final ParkingLot parkingLot;
    private final SpotAllocationStrategy allocationStrategy;
    private final PricingStrategy pricingStrategy;
    private final PaymentProcessor paymentProcessor;
    private final Clock clock;
    private final AtomicLong ticketSequence = new AtomicLong(1);
    private final Map<String, Ticket> activeTicketsById = new HashMap<>();
    private final Map<String, Ticket> closedTicketsById = new HashMap<>();
    private final Map<String, String> activeTicketIdsByLicensePlate = new HashMap<>();

    public ParkingLotService(ParkingLot parkingLot) {
        this(
                parkingLot,
                new NearestAvailableSpotStrategy(),
                HourlyPricingStrategy.defaultRates(),
                new InMemoryPaymentProcessor(),
                Clock.systemDefaultZone()
        );
    }

    public ParkingLotService(
            ParkingLot parkingLot,
            SpotAllocationStrategy allocationStrategy,
            PricingStrategy pricingStrategy,
            PaymentProcessor paymentProcessor,
            Clock clock
    ) {
        this.parkingLot = Objects.requireNonNull(parkingLot, "parkingLot must not be null");
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy, "allocationStrategy must not be null");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy must not be null");
        this.paymentProcessor = Objects.requireNonNull(paymentProcessor, "paymentProcessor must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public synchronized Ticket park(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        if (activeTicketIdsByLicensePlate.containsKey(vehicle.licensePlate())) {
            throw new VehicleAlreadyParkedException("vehicle is already parked: " + vehicle.licensePlate());
        }

        ParkingSpot spot = allocationStrategy.findSpot(parkingLot, vehicle)
                .orElseThrow(() -> new ParkingLotFullException("no available spot for " + vehicle.type()));

        spot.occupy(vehicle);
        Ticket ticket = new Ticket(nextTicketId(), vehicle, spot, clock.instant());
        activeTicketsById.put(ticket.ticketId(), ticket);
        activeTicketIdsByLicensePlate.put(vehicle.licensePlate(), ticket.ticketId());
        return ticket;
    }

    public synchronized ParkingReceipt unpark(String ticketId, PaymentMethod paymentMethod) {
        String normalizedTicketId = normalizeTicketId(ticketId);
        Ticket ticket = activeTicketsById.get(normalizedTicketId);
        if (ticket == null) {
            throw new InvalidTicketException("ticket is not active: " + normalizedTicketId);
        }

        Instant exitTime = clock.instant();
        Payment payment = paymentProcessor.charge(
                pricingStrategy.calculateFee(ticket, exitTime),
                paymentMethod,
                exitTime
        );

        ticket.parkingSpot().release();
        ticket.close(exitTime, payment.amount(), payment);
        activeTicketsById.remove(ticket.ticketId());
        activeTicketIdsByLicensePlate.remove(ticket.vehicle().licensePlate());
        closedTicketsById.put(ticket.ticketId(), ticket);

        return ParkingReceipt.from(ticket, payment);
    }

    public synchronized Optional<Ticket> findActiveTicket(String ticketId) {
        return Optional.ofNullable(activeTicketsById.get(normalizeTicketId(ticketId)));
    }

    public synchronized Optional<Ticket> findClosedTicket(String ticketId) {
        return Optional.ofNullable(closedTicketsById.get(normalizeTicketId(ticketId)));
    }

    public synchronized long activeTicketCount() {
        return activeTicketsById.size();
    }

    public synchronized long availableSpotCount(VehicleType vehicleType) {
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        return parkingLot.allSpots().stream()
                .filter(ParkingSpot::isAvailable)
                .filter(spot -> spot.type().canFit(vehicleType))
                .count();
    }

    public synchronized Map<SpotType, Long> availableSpotsByType() {
        EnumMap<SpotType, Long> counts = new EnumMap<>(SpotType.class);
        for (SpotType spotType : SpotType.values()) {
            counts.put(spotType, 0L);
        }

        parkingLot.allSpots().stream()
                .filter(ParkingSpot::isAvailable)
                .forEach(spot -> counts.merge(spot.type(), 1L, Long::sum));

        return Collections.unmodifiableMap(counts);
    }

    private String nextTicketId() {
        return "T-%06d".formatted(ticketSequence.getAndIncrement());
    }

    private static String normalizeTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new InvalidTicketException("ticketId must not be blank");
        }
        return ticketId.trim();
    }
}

