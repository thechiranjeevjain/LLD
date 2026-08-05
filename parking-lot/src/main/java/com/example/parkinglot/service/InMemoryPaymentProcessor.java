package com.example.parkinglot.service;

import com.example.parkinglot.domain.Payment;
import com.example.parkinglot.domain.PaymentMethod;
import com.example.parkinglot.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryPaymentProcessor implements PaymentProcessor {
    private final AtomicLong paymentSequence = new AtomicLong(1);

    @Override
    public Payment charge(BigDecimal amount, PaymentMethod method, Instant paidAt) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(paidAt, "paidAt must not be null");

        String paymentId = "P-%06d".formatted(paymentSequence.getAndIncrement());
        return new Payment(paymentId, amount, method, PaymentStatus.SUCCESS, paidAt);
    }
}

