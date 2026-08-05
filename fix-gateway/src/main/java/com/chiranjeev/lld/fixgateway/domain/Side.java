package com.chiranjeev.lld.fixgateway.domain;

import java.util.Arrays;

public enum Side {
    BUY("1"),
    SELL("2");

    private final String fixValue;

    Side(String fixValue) {
        this.fixValue = fixValue;
    }

    public String fixValue() {
        return fixValue;
    }

    public static Side fromFixValue(String value) {
        return Arrays.stream(values())
                .filter(side -> side.fixValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Side: " + value));
    }
}

