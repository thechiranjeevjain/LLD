package com.example.parkinglot.service;

import com.example.parkinglot.domain.Payment;
import com.example.parkinglot.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentProcessor {
    Payment charge(BigDecimal amount, PaymentMethod method, Instant paidAt);
}

