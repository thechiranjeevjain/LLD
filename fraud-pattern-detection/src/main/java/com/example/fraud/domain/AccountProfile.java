package com.example.fraud.domain;

import java.util.Objects;
import java.util.Set;

public record AccountProfile(
        String accountId,
        String homeCountry,
        Set<String> knownDeviceIds,
        Set<String> trustedMerchantIds
) {
    public AccountProfile {
        accountId = requireText(accountId, "accountId");
        homeCountry = requireText(homeCountry, "homeCountry").toUpperCase();
        knownDeviceIds = Set.copyOf(Objects.requireNonNull(knownDeviceIds, "knownDeviceIds"));
        trustedMerchantIds = Set.copyOf(Objects.requireNonNull(trustedMerchantIds, "trustedMerchantIds"));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
