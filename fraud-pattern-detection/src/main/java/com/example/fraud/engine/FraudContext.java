package com.example.fraud.engine;

import com.example.fraud.domain.AccountProfile;
import com.example.fraud.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record FraudContext(
        Optional<AccountProfile> accountProfile,
        List<Transaction> recentTransactions,
        Set<String> blacklistedMerchantIds,
        Set<String> blacklistedIpAddresses
) {
    public FraudContext {
        recentTransactions = List.copyOf(recentTransactions);
        blacklistedMerchantIds = Set.copyOf(blacklistedMerchantIds);
        blacklistedIpAddresses = Set.copyOf(blacklistedIpAddresses);
    }

    public Optional<Transaction> lastTransaction() {
        if (recentTransactions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(recentTransactions.get(recentTransactions.size() - 1));
    }
}
