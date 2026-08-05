package com.example.splitwise.domain;

import java.util.Objects;

public record ExpenseShare(String userId, Money amount) {

    public ExpenseShare {
        if (Objects.requireNonNull(userId, "userId").isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        Objects.requireNonNull(amount, "amount");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("share amount must be positive");
        }
    }
}
