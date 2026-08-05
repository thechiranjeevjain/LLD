package com.example.bookmyshow.domain;

import java.time.Duration;
import java.util.Objects;

public record Movie(String id, String title, String language, Duration duration) {

    public Movie {
        id = requireText(id, "id");
        title = requireText(title, "title");
        language = requireText(language, "language");
        duration = Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
