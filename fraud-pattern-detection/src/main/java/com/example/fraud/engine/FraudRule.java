package com.example.fraud.engine;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;

import java.util.Optional;

public interface FraudRule {
    Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context);
}
