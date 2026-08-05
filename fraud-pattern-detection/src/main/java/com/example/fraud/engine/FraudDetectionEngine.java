package com.example.fraud.engine;

import com.example.fraud.domain.FraudDecision;
import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.rules.BlacklistedEntityRule;
import com.example.fraud.rules.ForeignCountryRule;
import com.example.fraud.rules.HighAmountRule;
import com.example.fraud.rules.ImpossibleTravelRule;
import com.example.fraud.rules.MerchantBurstRule;
import com.example.fraud.rules.NewDeviceRule;
import com.example.fraud.rules.VelocityRule;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FraudDetectionEngine {
    private static final Duration DEFAULT_LOOKBACK = Duration.ofHours(24);

    private final FraudStateStore stateStore;
    private final List<FraudRule> rules;
    private final ScoringPolicy scoringPolicy;
    private final Clock clock;

    public FraudDetectionEngine(
            FraudStateStore stateStore,
            List<FraudRule> rules,
            ScoringPolicy scoringPolicy,
            Clock clock
    ) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.rules = List.copyOf(rules);
        this.scoringPolicy = Objects.requireNonNull(scoringPolicy, "scoringPolicy");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static FraudDetectionEngine withDefaultRules(FraudStateStore stateStore) {
        List<FraudRule> rules = List.of(
                new BlacklistedEntityRule(),
                new ImpossibleTravelRule(Duration.ofHours(2)),
                new HighAmountRule(new BigDecimal("50000.00"), 35),
                new VelocityRule(Duration.ofMinutes(10), 3, 40),
                new MerchantBurstRule(Duration.ofMinutes(15), 2, 25),
                new ForeignCountryRule(25),
                new NewDeviceRule(15)
        );
        return new FraudDetectionEngine(
                stateStore,
                rules,
                new ScoringPolicy(40, 70),
                Clock.systemUTC()
        );
    }

    public FraudDecision analyze(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        FraudContext context = stateStore.buildContext(transaction.accountId(), transaction.occurredAt(), DEFAULT_LOOKBACK);
        List<RuleMatch> matches = new ArrayList<>();

        for (FraudRule rule : rules) {
            rule.evaluate(transaction, context).ifPresent(matches::add);
        }

        int riskScore = matches.stream()
                .mapToInt(RuleMatch::score)
                .sum();
        riskScore = Math.min(100, riskScore);

        FraudDecision decision = new FraudDecision(
                transaction.transactionId(),
                scoringPolicy.decide(riskScore),
                riskScore,
                matches,
                clock.instant()
        );
        stateStore.record(transaction);
        return decision;
    }
}
