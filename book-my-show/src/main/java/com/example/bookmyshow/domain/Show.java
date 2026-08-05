package com.example.bookmyshow.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record Show(
        String id,
        Movie movie,
        Theatre theatre,
        Screen screen,
        Instant startTime,
        Map<SeatType, Money> pricesBySeatType
) {

    public Show {
        id = requireText(id, "id");
        movie = Objects.requireNonNull(movie, "movie");
        theatre = Objects.requireNonNull(theatre, "theatre");
        screen = Objects.requireNonNull(screen, "screen");
        startTime = Objects.requireNonNull(startTime, "startTime");
        if (pricesBySeatType == null || pricesBySeatType.isEmpty()) {
            throw new IllegalArgumentException("pricesBySeatType must not be empty");
        }
        pricesBySeatType = Map.copyOf(pricesBySeatType);
        for (Seat seat : screen.seats()) {
            if (!pricesBySeatType.containsKey(seat.type())) {
                throw new IllegalArgumentException("missing price for seat type: " + seat.type());
            }
        }
    }

    public Money priceFor(Seat seat) {
        return pricesBySeatType.get(seat.type());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
