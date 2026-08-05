package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.util.Optional;

public class ForeignCountryRule implements FraudRule {
    private final int score;

    public ForeignCountryRule(int score) {
        this.score = score;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        return context.accountProfile()
                .filter(profile -> !profile.homeCountry().equals(transaction.country()))
                .map(profile -> new RuleMatch(
                        "FOREIGN_COUNTRY",
                        score,
                        "Transaction country differs from account home country"
                ));
    }
}
