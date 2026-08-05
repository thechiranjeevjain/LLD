package com.example.fraud.engine;

import com.example.fraud.domain.AccountProfile;
import com.example.fraud.domain.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InMemoryFraudStateStore implements FraudStateStore {
    private final Map<String, AccountProfile> profilesByAccountId = new HashMap<>();
    private final Map<String, List<Transaction>> transactionsByAccountId = new HashMap<>();
    private final Set<String> blacklistedMerchantIds = new HashSet<>();
    private final Set<String> blacklistedIpAddresses = new HashSet<>();

    @Override
    public FraudContext buildContext(String accountId, Instant asOf, Duration lookback) {
        Instant earliest = asOf.minus(lookback);
        List<Transaction> recent = transactionsByAccountId
                .getOrDefault(accountId, List.of())
                .stream()
                .filter(transaction -> !transaction.occurredAt().isBefore(earliest))
                .filter(transaction -> !transaction.occurredAt().isAfter(asOf))
                .sorted(Comparator.comparing(Transaction::occurredAt))
                .toList();

        return new FraudContext(
                findProfile(accountId),
                recent,
                blacklistedMerchantIds,
                blacklistedIpAddresses
        );
    }

    @Override
    public void record(Transaction transaction) {
        transactionsByAccountId
                .computeIfAbsent(transaction.accountId(), ignored -> new ArrayList<>())
                .add(transaction);
    }

    @Override
    public Optional<AccountProfile> findProfile(String accountId) {
        return Optional.ofNullable(profilesByAccountId.get(accountId));
    }

    public void saveProfile(AccountProfile profile) {
        profilesByAccountId.put(profile.accountId(), profile);
    }

    public void blacklistMerchant(String merchantId) {
        blacklistedMerchantIds.add(merchantId);
    }

    public void blacklistIpAddress(String ipAddress) {
        blacklistedIpAddresses.add(ipAddress);
    }
}
