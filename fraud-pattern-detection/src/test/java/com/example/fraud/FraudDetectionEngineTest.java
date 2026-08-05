package com.example.fraud;

import com.example.fraud.domain.AccountProfile;
import com.example.fraud.domain.Channel;
import com.example.fraud.domain.DecisionType;
import com.example.fraud.domain.FraudDecision;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudDetectionEngine;
import com.example.fraud.engine.InMemoryFraudStateStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudDetectionEngineTest {
    private static final Instant BASE_TIME = Instant.parse("2026-08-05T10:00:00Z");

    @Test
    void approvesKnownLowRiskTransaction() {
        TestFixture fixture = fixture();

        FraudDecision decision = fixture.engine.analyze(transaction("T-1", "M-GROCERY", "D-1", "IN", "500.00", BASE_TIME));

        assertEquals(DecisionType.APPROVE, decision.decisionType());
        assertEquals(0, decision.riskScore());
        assertTrue(decision.matches().isEmpty());
    }

    @Test
    void reviewsHighAmountTransactionFromNewDevice() {
        TestFixture fixture = fixture();

        FraudDecision decision = fixture.engine.analyze(transaction("T-1", "M-GROCERY", "D-2", "IN", "75000.00", BASE_TIME));

        assertEquals(DecisionType.REVIEW, decision.decisionType());
        assertEquals(50, decision.riskScore());
        assertRuleMatched(decision, "HIGH_AMOUNT");
        assertRuleMatched(decision, "NEW_DEVICE");
    }

    @Test
    void flagsVelocityOnThirdTransactionInsideWindow() {
        TestFixture fixture = fixture();

        fixture.engine.analyze(transaction("T-1", "M-GROCERY", "D-1", "IN", "100.00", BASE_TIME));
        fixture.engine.analyze(transaction("T-2", "M-FUEL", "D-1", "IN", "150.00", BASE_TIME.plusSeconds(120)));
        FraudDecision thirdDecision = fixture.engine.analyze(transaction("T-3", "M-PHARMACY", "D-1", "IN", "200.00", BASE_TIME.plusSeconds(240)));

        assertEquals(DecisionType.REVIEW, thirdDecision.decisionType());
        assertEquals(40, thirdDecision.riskScore());
        assertRuleMatched(thirdDecision, "VELOCITY");
    }

    @Test
    void blocksBlacklistedMerchantImmediately() {
        TestFixture fixture = fixture();
        fixture.stateStore.blacklistMerchant("M-STOLEN");

        FraudDecision decision = fixture.engine.analyze(transaction("T-1", "M-STOLEN", "D-1", "IN", "100.00", BASE_TIME));

        assertEquals(DecisionType.BLOCK, decision.decisionType());
        assertEquals(80, decision.riskScore());
        assertRuleMatched(decision, "BLACKLISTED_MERCHANT");
    }

    @Test
    void blocksImpossibleTravelWithForeignCountrySignal() {
        TestFixture fixture = fixture();
        fixture.engine.analyze(transaction("T-1", "M-GROCERY", "D-1", "IN", "100.00", BASE_TIME));

        FraudDecision decision = fixture.engine.analyze(transaction("T-2", "M-TRAVEL", "D-1", "US", "300.00", BASE_TIME.plusSeconds(600)));

        assertEquals(DecisionType.BLOCK, decision.decisionType());
        assertEquals(85, decision.riskScore());
        assertRuleMatched(decision, "IMPOSSIBLE_TRAVEL");
        assertRuleMatched(decision, "FOREIGN_COUNTRY");
    }

    @Test
    void flagsMerchantBurstOnRepeatedMerchantInsideWindow() {
        TestFixture fixture = fixture();

        fixture.engine.analyze(transaction("T-1", "M-APP", "D-1", "IN", "100.00", BASE_TIME));
        FraudDecision secondDecision = fixture.engine.analyze(transaction("T-2", "M-APP", "D-1", "IN", "125.00", BASE_TIME.plusSeconds(300)));

        assertEquals(DecisionType.APPROVE, secondDecision.decisionType());
        assertEquals(25, secondDecision.riskScore());
        assertRuleMatched(secondDecision, "MERCHANT_BURST");
    }

    private static TestFixture fixture() {
        InMemoryFraudStateStore stateStore = new InMemoryFraudStateStore();
        stateStore.saveProfile(new AccountProfile(
                "A-100",
                "IN",
                Set.of("D-1"),
                Set.of("M-GROCERY")
        ));
        return new TestFixture(stateStore, FraudDetectionEngine.withDefaultRules(stateStore));
    }

    private static Transaction transaction(
            String id,
            String merchantId,
            String deviceId,
            String country,
            String amount,
            Instant occurredAt
    ) {
        return new Transaction(
                id,
                "A-100",
                merchantId,
                new BigDecimal(amount),
                "INR",
                country,
                Channel.CARD_NOT_PRESENT,
                deviceId,
                "198.51.100.1",
                occurredAt
        );
    }

    private static void assertRuleMatched(FraudDecision decision, String ruleName) {
        assertTrue(
                decision.matches().stream().anyMatch(match -> match.ruleName().equals(ruleName)),
                () -> "Expected rule " + ruleName + " in " + decision.matches()
        );
    }

    private record TestFixture(InMemoryFraudStateStore stateStore, FraudDetectionEngine engine) {
    }
}
