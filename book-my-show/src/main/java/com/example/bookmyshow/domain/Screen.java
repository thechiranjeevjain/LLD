package com.example.bookmyshow.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Screen(String id, String name, List<Seat> seats) {

    public Screen {
        id = requireText(id, "id");
        name = requireText(name, "name");
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("seats must not be empty");
        }
        seats = List.copyOf(seats);
        Set<String> ids = new HashSet<>();
        for (Seat seat : seats) {
            if (!ids.add(seat.id())) {
                throw new IllegalArgumentException("duplicate seat id: " + seat.id());
            }
        }
    }

    public Optional<Seat> seatById(String seatId) {
        return seats.stream()
                .filter(seat -> seat.id().equals(seatId))
                .findFirst();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
