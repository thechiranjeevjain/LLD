package com.example.designredis.store;

public enum RedisType {
    STRING("string"),
    LIST("list"),
    HASH("hash");

    private final String wireName;

    RedisType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}
