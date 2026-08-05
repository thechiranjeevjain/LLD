package com.example.fraud.rules;

import com.example.fraud.domain.RuleMatch;
import com.example.fraud.domain.Transaction;
import com.example.fraud.engine.FraudContext;
import com.example.fraud.engine.FraudRule;

import java.util.Optional;

public class BlacklistedEntityRule implements FraudRule {
    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, FraudContext context) {
        if (context.blacklistedMerchantIds().contains(transaction.merchantId())) {
            return Optional.of(new RuleMatch(
                    "BLACKLISTED_MERCHANT",
                    80,
                    "Merchant is blacklisted"
            ));
        }
        if (context.blacklistedIpAddresses().contains(transaction.ipAddress())) {
            return Optional.of(new RuleMatch(
                    "BLACKLISTED_IP",
                    80,
                    "IP address is blacklisted"
            ));
        }
        return Optional.empty();
    }
}
