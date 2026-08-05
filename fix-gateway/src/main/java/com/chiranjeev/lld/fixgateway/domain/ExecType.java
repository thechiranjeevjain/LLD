package com.chiranjeev.lld.fixgateway.domain;

import java.util.Arrays;

public enum ExecType {
    NEW("0"),
    REJECTED("8");

    private final String fixValue;

    ExecType(String fixValue) {
        this.fixValue = fixValue;
    }

    public String fixValue() {
        return fixValue;
    }

    public static ExecType fromFixValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.fixValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported ExecType: " + value));
    }
}

