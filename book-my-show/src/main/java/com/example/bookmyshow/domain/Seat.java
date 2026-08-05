package com.example.bookmyshow.domain;

import java.util.Objects;

public record Seat(String id, String row, int number, SeatType type) {

    public Seat {
        id = requireText(id, "id");
        row = requireText(row, "row");
        if (number <= 0) {
            throw new IllegalArgumentException("number must be positive");
        }
        type = Objects.requireNonNull(type, "type");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
