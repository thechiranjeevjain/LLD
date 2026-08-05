package com.example.fraud.engine;

import com.example.fraud.domain.AccountProfile;
import com.example.fraud.domain.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface FraudStateStore {
    FraudContext buildContext(String accountId, Instant asOf, Duration lookback);

    void record(Transaction transaction);

    Optional<AccountProfile> findProfile(String accountId);
}
