package com.example.fraud;

import com.example.fraud.domain.AccountProfile;
import com.example.fraud.domain.Channel;
import com.example.fraud.domain.FraudDecision;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudDetectionEngine;
import com.example.fraud.engine.InMemoryFraudStateStore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class FraudPatternDetectionDemo {
    public static void main(String[] args) {
        InMemoryFraudStateStore stateStore = new InMemoryFraudStateStore();
        stateStore.saveProfile(new AccountProfile(
                "A-100",
                "IN",
                Set.of("D-1"),
                Set.of("M-GROCERY", "M-FUEL")
        ));
        stateStore.blacklistMerchant("M-STOLEN");
        stateStore.blacklistIpAddress("203.0.113.9");

        FraudDetectionEngine engine = FraudDetectionEngine.withDefaultRules(stateStore);
        Instant baseTime = Instant.parse("2026-08-05T10:00:00Z");

        List<Transaction> transactions = List.of(
                transaction("T-001", "A-100", "M-GROCERY", "D-1", "198.51.100.1", "IN", "450.00", baseTime),
                transaction("T-002", "A-100", "M-ELECTRONICS", "D-7", "198.51.100.2", "IN", "65000.00", baseTime.plusSeconds(60)),
                transaction("T-003", "A-100", "M-STOLEN", "D-7", "203.0.113.9", "US", "1200.00", baseTime.plusSeconds(180)),
                transaction("T-004", "A-100", "M-TRAVEL", "D-1", "198.51.100.3", "FR", "900.00", baseTime.plusSeconds(900))
        );

        for (Transaction transaction : transactions) {
            FraudDecision decision = engine.analyze(transaction);
            System.out.printf("%s -> %s score=%d matches=%s%n",
                    decision.transactionId(),
                    decision.decisionType(),
                    decision.riskScore(),
                    decision.matches().stream().map(match -> match.ruleName()).toList());
        }
    }

    private static Transaction transaction(
            String id,
            String accountId,
            String merchantId,
            String deviceId,
            String ipAddress,
            String country,
            String amount,
            Instant occurredAt
    ) {
        return new Transaction(
                id,
                accountId,
                merchantId,
                new BigDecimal(amount),
                "INR",
                country,
                Channel.CARD_PRESENT,
                deviceId,
                ipAddress,
                occurredAt
        );
    }
}
