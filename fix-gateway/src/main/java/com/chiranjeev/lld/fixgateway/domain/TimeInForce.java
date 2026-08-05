package com.chiranjeev.lld.fixgateway.domain;

import java.util.Arrays;

public enum TimeInForce {
    DAY("0"),
    IMMEDIATE_OR_CANCEL("3"),
    FILL_OR_KILL("4");

    private final String fixValue;

    TimeInForce(String fixValue) {
        this.fixValue = fixValue;
    }

    public String fixValue() {
        return fixValue;
    }

    public static TimeInForce fromFixValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.fixValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported TimeInForce: " + value));
    }
}

