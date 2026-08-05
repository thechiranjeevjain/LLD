package com.example.bookmyshow.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Booking {
    private final String id;
    private final String userId;
    private final String showId;
    private final List<String> seatIds;
    private final Money amount;
    private final Instant createdAt;
    private final Instant holdExpiresAt;
    private BookingStatus status;
    private Instant confirmedAt;
    private String paymentId;

    public Booking(
            String id,
            String userId,
            String showId,
            List<String> seatIds,
            Money amount,
            Instant createdAt,
            Instant holdExpiresAt
    ) {
        this.id = requireText(id, "id");
        this.userId = requireText(userId, "userId");
        this.showId = requireText(showId, "showId");
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("seatIds must not be empty");
        }
        this.seatIds = List.copyOf(seatIds);
        this.amount = Objects.requireNonNull(amount, "amount");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.holdExpiresAt = Objects.requireNonNull(holdExpiresAt, "holdExpiresAt");
        this.status = BookingStatus.PENDING;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String showId() {
        return showId;
    }

    public List<String> seatIds() {
        return seatIds;
    }

    public Money amount() {
        return amount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant holdExpiresAt() {
        return holdExpiresAt;
    }

    public BookingStatus status() {
        return status;
    }

    public Instant confirmedAt() {
        return confirmedAt;
    }

    public String paymentId() {
        return paymentId;
    }

    public boolean isHoldExpired(Instant now) {
        return status == BookingStatus.PENDING && !holdExpiresAt.isAfter(now);
    }

    public void confirm(String paymentId, Instant confirmedAt) {
        ensurePending();
        this.paymentId = requireText(paymentId, "paymentId");
        this.confirmedAt = Objects.requireNonNull(confirmedAt, "confirmedAt");
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING) {
            this.status = BookingStatus.CANCELLED;
        }
    }

    public void expire() {
        if (status == BookingStatus.PENDING) {
            this.status = BookingStatus.EXPIRED;
        }
    }

    private void ensurePending() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("booking must be pending");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
