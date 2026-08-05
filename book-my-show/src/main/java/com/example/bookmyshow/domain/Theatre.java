package com.example.bookmyshow.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Theatre(String id, String name, String city, List<Screen> screens) {

    public Theatre {
        id = requireText(id, "id");
        name = requireText(name, "name");
        city = requireText(city, "city");
        if (screens == null || screens.isEmpty()) {
            throw new IllegalArgumentException("screens must not be empty");
        }
        screens = List.copyOf(screens);
        Set<String> ids = new HashSet<>();
        for (Screen screen : screens) {
            if (!ids.add(screen.id())) {
                throw new IllegalArgumentException("duplicate screen id: " + screen.id());
            }
        }
    }

    public Optional<Screen> screenById(String screenId) {
        return screens.stream()
                .filter(screen -> screen.id().equals(screenId))
                .findFirst();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
