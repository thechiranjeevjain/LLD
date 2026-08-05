package com.example.splitwise.service;

import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ExactSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Money> split(Money total, List<SplitInput> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("exact split requires at least one participant");
        }

        Map<String, Money> shares = new LinkedHashMap<>();
        Money sum = Money.zero(total.currency());
        for (SplitInput input : inputs) {
            if (shares.containsKey(input.userId())) {
                throw new IllegalArgumentException("duplicate split participant: " + input.userId());
            }
            Money amount = Objects.requireNonNull(input.exactAmount(), "exact amount is required");
            if (!amount.isPositive()) {
                throw new IllegalArgumentException("exact amount must be positive");
            }
            sum = sum.plus(amount);
            shares.put(input.userId(), amount);
        }

        if (!sum.equals(total)) {
            throw new IllegalArgumentException("exact split total " + sum + " does not match expense total " + total);
        }
        return shares;
    }
}
