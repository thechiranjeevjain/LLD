package com.example.splitwise.domain;

import java.util.Objects;

public record User(String id, String name, String email) {

    public User {
        if (Objects.requireNonNull(id, "id").isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (Objects.requireNonNull(email, "email").isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
    }
}
