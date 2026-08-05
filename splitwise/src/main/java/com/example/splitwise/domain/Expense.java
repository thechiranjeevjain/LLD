package com.example.splitwise.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Expense(
        String id,
        String groupId,
        String paidByUserId,
        Money amount,
        String description,
        SplitType splitType,
        List<ExpenseShare> shares,
        Instant createdAt
) {

    public Expense {
        if (Objects.requireNonNull(id, "id").isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (Objects.requireNonNull(groupId, "groupId").isBlank()) {
            throw new IllegalArgumentException("groupId is required");
        }
        if (Objects.requireNonNull(paidByUserId, "paidByUserId").isBlank()) {
            throw new IllegalArgumentException("paidByUserId is required");
        }
        Objects.requireNonNull(amount, "amount");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("expense amount must be positive");
        }
        description = Objects.requireNonNullElse(description, "");
        Objects.requireNonNull(splitType, "splitType");
        shares = List.copyOf(Objects.requireNonNull(shares, "shares"));
        if (shares.isEmpty()) {
            throw new IllegalArgumentException("expense must have at least one share");
        }
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }
}
