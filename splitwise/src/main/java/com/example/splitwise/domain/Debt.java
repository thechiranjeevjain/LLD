package com.example.splitwise.domain;

import java.util.Objects;

public record Debt(String fromUserId, String toUserId, Money amount) {

    public Debt {
        if (Objects.requireNonNull(fromUserId, "fromUserId").isBlank()) {
            throw new IllegalArgumentException("fromUserId is required");
        }
        if (Objects.requireNonNull(toUserId, "toUserId").isBlank()) {
            throw new IllegalArgumentException("toUserId is required");
        }
        Objects.requireNonNull(amount, "amount");
        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("debt must be between two different users");
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("debt amount must be positive");
        }
    }

    @Override
    public String toString() {
        return fromUserId + " owes " + toUserId + " " + amount;
    }
}
