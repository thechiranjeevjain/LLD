package com.example.parkinglot.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

public record Payment(
        String paymentId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        Instant paidAt
) {
    public Payment {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId must not be blank");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        method = Objects.requireNonNull(method, "method must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        paidAt = Objects.requireNonNull(paidAt, "paidAt must not be null");
    }
}

