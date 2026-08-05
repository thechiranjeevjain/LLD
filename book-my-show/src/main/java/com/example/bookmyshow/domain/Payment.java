package com.example.bookmyshow.domain;

import java.time.Instant;
import java.util.Objects;

public record Payment(
        String id,
        String bookingId,
        Money amount,
        PaymentStatus status,
        Instant paidAt,
        String reference
) {

    public Payment {
        id = requireText(id, "id");
        bookingId = requireText(bookingId, "bookingId");
        amount = Objects.requireNonNull(amount, "amount");
        status = Objects.requireNonNull(status, "status");
        paidAt = Objects.requireNonNull(paidAt, "paidAt");
        reference = requireText(reference, "reference");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
