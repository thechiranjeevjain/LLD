package com.example.splitwise.service;

import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EqualSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Money> split(Money total, List<SplitInput> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("equal split requires at least one participant");
        }

        long totalMinorUnits = total.toMinorUnits();
        long baseShare = totalMinorUnits / inputs.size();
        long remainder = totalMinorUnits % inputs.size();

        Map<String, Money> shares = new LinkedHashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            SplitInput input = inputs.get(i);
            ensureUnique(shares, input.userId());
            long minorUnits = baseShare + (i < remainder ? 1 : 0);
            shares.put(input.userId(), Money.fromMinorUnits(total.currency(), minorUnits));
        }
        return shares;
    }

    private static void ensureUnique(Map<String, Money> shares, String userId) {
        if (shares.containsKey(userId)) {
            throw new IllegalArgumentException("duplicate split participant: " + userId);
        }
    }
}
