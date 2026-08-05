package com.example.splitwise.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record SplitInput(String userId, Money exactAmount, BigDecimal percentage) {

    public SplitInput {
        if (Objects.requireNonNull(userId, "userId").isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    public static SplitInput forUser(String userId) {
        return new SplitInput(userId, null, null);
    }

    public static SplitInput exact(String userId, Money amount) {
        return new SplitInput(userId, Objects.requireNonNull(amount, "amount"), null);
    }

    public static SplitInput percentage(String userId, BigDecimal percentage) {
        Objects.requireNonNull(percentage, "percentage");
        if (percentage.signum() <= 0) {
            throw new IllegalArgumentException("percentage must be positive");
        }
        return new SplitInput(userId, null, percentage);
    }
}
