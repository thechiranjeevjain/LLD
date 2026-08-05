package com.example.splitwise.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Group {
    private final String id;
    private final String name;
    private final Set<String> memberIds = new LinkedHashSet<>();

    public Group(String id, String name, List<String> memberIds) {
        if (Objects.requireNonNull(id, "id").isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        this.id = id;
        this.name = name;
        Objects.requireNonNull(memberIds, "memberIds").forEach(this::addMember);
        if (this.memberIds.isEmpty()) {
            throw new IllegalArgumentException("group must have at least one member");
        }
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Set<String> memberIds() {
        return Set.copyOf(memberIds);
    }

    public boolean hasMember(String userId) {
        return memberIds.contains(userId);
    }

    public void addMember(String userId) {
        if (Objects.requireNonNull(userId, "userId").isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        memberIds.add(userId);
    }
}
