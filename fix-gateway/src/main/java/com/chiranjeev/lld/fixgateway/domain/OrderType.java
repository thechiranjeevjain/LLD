package com.chiranjeev.lld.fixgateway.domain;

import java.util.Arrays;

public enum OrderType {
    MARKET("1"),
    LIMIT("2");

    private final String fixValue;

    OrderType(String fixValue) {
        this.fixValue = fixValue;
    }

    public String fixValue() {
        return fixValue;
    }

    public static OrderType fromFixValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.fixValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OrdType: " + value));
    }
}

