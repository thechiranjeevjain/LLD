package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.time.Duration;
import java.util.Optional;

public class ImpossibleTravelRule implements FraudRule {
    private final Duration minimumTravelTime;

    public ImpossibleTravelRule(Duration minimumTravelTime) {
        this.minimumTravelTime = minimumTravelTime;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        return context.lastTransaction()
                .filter(previous -> !previous.country().equals(transaction.country()))
                .filter(previous -> Duration.between(previous.occurredAt(), transaction.occurredAt()).compareTo(minimumTravelTime) < 0)
                .map(previous -> new RuleMatch(
                        "IMPOSSIBLE_TRAVEL",
                        60,
                        "Country changed from " + previous.country() + " to " + transaction.country()
                ));
    }
}
