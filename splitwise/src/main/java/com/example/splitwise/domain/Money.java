package com.example.splitwise.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private static final int SCALE = 2;
    private static final BigDecimal MINOR_UNITS = BigDecimal.valueOf(100);

    private final String currency;
    private final BigDecimal amount;

    private Money(String currency, BigDecimal amount) {
        if (Objects.requireNonNull(currency, "currency").isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        this.currency = Currency.getInstance(currency).getCurrencyCode();
        this.amount = Objects.requireNonNull(amount, "amount").setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(String currency, String amount) {
        return new Money(currency, new BigDecimal(amount));
    }

    public static Money of(String currency, BigDecimal amount) {
        return new Money(currency, amount);
    }

    public static Money zero(String currency) {
        return new Money(currency, BigDecimal.ZERO);
    }

    public static Money fromMinorUnits(String currency, long minorUnits) {
        return new Money(currency, BigDecimal.valueOf(minorUnits).divide(MINOR_UNITS, SCALE, RoundingMode.UNNECESSARY));
    }

    public String currency() {
        return currency;
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(currency, amount.add(other.amount));
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(currency, amount.subtract(other.amount));
    }

    public Money negate() {
        return new Money(currency, amount.negate());
    }

    public Money abs() {
        return amount.signum() < 0 ? negate() : this;
    }

    public long toMinorUnits() {
        return amount.movePointRight(SCALE).longValueExact();
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public Money min(Money other) {
        return compareTo(other) <= 0 ? this : other;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return currency.equals(money.currency) && amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(Objects.requireNonNull(other, "other").currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
