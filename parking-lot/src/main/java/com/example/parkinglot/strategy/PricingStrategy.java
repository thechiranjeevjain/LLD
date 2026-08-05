package com.example.parkinglot.strategy;

import com.example.parkinglot.domain.Ticket;

import java.math.BigDecimal;
import java.time.Instant;

public interface PricingStrategy {
    BigDecimal calculateFee(Ticket ticket, Instant exitTime);
}

