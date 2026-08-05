package com.example.bookmyshow.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public record Money(String currency, BigDecimal amount) {

    public Money {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        currency = currency.toUpperCase(Locale.ROOT);
        amount = Objects.requireNonNull(amount, "amount").setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static Money of(String currency, double amount) {
        return new Money(currency, BigDecimal.valueOf(amount));
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("cannot add different currencies");
        }
        return new Money(currency, amount.add(other.amount));
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
