package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class VelocityRule implements FraudRule {
    private final Duration window;
    private final int maxTransactionsIncludingCurrent;
    private final int score;

    public VelocityRule(Duration window, int maxTransactionsIncludingCurrent, int score) {
        if (maxTransactionsIncludingCurrent < 2) {
            throw new IllegalArgumentException("maxTransactionsIncludingCurrent must be at least 2");
        }
        this.window = window;
        this.maxTransactionsIncludingCurrent = maxTransactionsIncludingCurrent;
        this.score = score;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        Instant windowStart = transaction.occurredAt().minus(window);
        long previousCount = context.recentTransactions()
                .stream()
                .filter(previous -> !previous.occurredAt().isBefore(windowStart))
                .count();

        long totalIncludingCurrent = previousCount + 1;
        if (totalIncludingCurrent < maxTransactionsIncludingCurrent) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(
                "VELOCITY",
                score,
                totalIncludingCurrent + " transactions within " + window.toMinutes() + " minutes"
        ));
    }
}
