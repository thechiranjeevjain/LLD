package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.math.BigDecimal;
import java.util.Optional;

public class HighAmountRule implements FraudRule {
    private final BigDecimal threshold;
    private final int score;

    public HighAmountRule(BigDecimal threshold, int score) {
        this.threshold = threshold;
        this.score = score;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        if (transaction.amount().compareTo(threshold) < 0) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(
                "HIGH_AMOUNT",
                score,
                "Amount is greater than or equal to " + threshold
        ));
    }
}
