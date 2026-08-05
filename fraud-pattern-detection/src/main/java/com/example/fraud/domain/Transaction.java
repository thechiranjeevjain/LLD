package com.example.fraud.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String transactionId,
        String accountId,
        String merchantId,
        BigDecimal amount,
        String currency,
        String country,
        Channel channel,
        String deviceId,
        String ipAddress,
        Instant occurredAt
) {
    public Transaction {
        transactionId = requireText(transactionId, "transactionId");
        accountId = requireText(accountId, "accountId");
        merchantId = requireText(merchantId, "merchantId");
        currency = requireText(currency, "currency").toUpperCase();
        country = requireText(country, "country").toUpperCase();
        deviceId = requireText(deviceId, "deviceId");
        ipAddress = requireText(ipAddress, "ipAddress");
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel is required");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
