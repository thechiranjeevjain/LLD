package com.chiranjeev.lld.fixgateway.domain;

import java.util.Arrays;

public enum OrderStatus {
    NEW("0"),
    REJECTED("8");

    private final String fixValue;

    OrderStatus(String fixValue) {
        this.fixValue = fixValue;
    }

    public String fixValue() {
        return fixValue;
    }

    public static OrderStatus fromFixValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.fixValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OrderStatus: " + value));
    }
}

