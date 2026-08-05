package com.example.parkinglot.strategy;

import com.example.parkinglot.domain.Ticket;
import com.example.parkinglot.domain.VehicleType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class HourlyPricingStrategy implements PricingStrategy {
    private final EnumMap<VehicleType, BigDecimal> hourlyRates;

    public HourlyPricingStrategy(Map<VehicleType, BigDecimal> hourlyRates) {
        Objects.requireNonNull(hourlyRates, "hourlyRates must not be null");

        this.hourlyRates = new EnumMap<>(VehicleType.class);
        for (VehicleType vehicleType : VehicleType.values()) {
            BigDecimal rate = hourlyRates.get(vehicleType);
            if (rate == null) {
                throw new IllegalArgumentException("missing rate for " + vehicleType);
            }
            if (rate.signum() < 0) {
                throw new IllegalArgumentException("rate must not be negative");
            }
            this.hourlyRates.put(vehicleType, rate.setScale(2, RoundingMode.HALF_UP));
        }
    }

    public static HourlyPricingStrategy defaultRates() {
        return new HourlyPricingStrategy(Map.of(
                VehicleType.MOTORCYCLE, new BigDecimal("10.00"),
                VehicleType.CAR, new BigDecimal("20.00"),
                VehicleType.ELECTRIC_CAR, new BigDecimal("25.00"),
                VehicleType.TRUCK, new BigDecimal("30.00")
        ));
    }

    @Override
    public BigDecimal calculateFee(Ticket ticket, Instant exitTime) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(exitTime, "exitTime must not be null");
        if (exitTime.isBefore(ticket.entryTime())) {
            throw new IllegalArgumentException("exitTime must not be before entryTime");
        }

        long parkedSeconds = Duration.between(ticket.entryTime(), exitTime).getSeconds();
        long chargedHours = Math.max(1, (parkedSeconds + 3_599) / 3_600);
        BigDecimal hourlyRate = hourlyRates.get(ticket.vehicle().type());
        return hourlyRate.multiply(BigDecimal.valueOf(chargedHours)).setScale(2, RoundingMode.HALF_UP);
    }
}
