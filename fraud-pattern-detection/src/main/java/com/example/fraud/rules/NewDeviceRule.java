package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.util.Optional;

public class NewDeviceRule implements FraudRule {
    private final int score;

    public NewDeviceRule(int score) {
        this.score = score;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        return context.accountProfile()
                .filter(profile -> !profile.knownDeviceIds().contains(transaction.deviceId()))
                .map(profile -> new RuleMatch(
                        "NEW_DEVICE",
                        score,
                        "Device is not known for account"
                ));
    }
}
