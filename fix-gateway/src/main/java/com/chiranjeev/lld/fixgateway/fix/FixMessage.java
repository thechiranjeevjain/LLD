package com.chiranjeev.lld.fixgateway.fix;

import com.chiranjeev.lld.fixgateway.gateway.FixValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class FixMessage {
    private final LinkedHashMap<Integer, String> fields;

    public FixMessage(Map<Integer, String> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("FIX message must contain at least one field");
        }
        this.fields = new LinkedHashMap<>(fields);
    }

    public static Builder builder(String messageType) {
        return new Builder().put(FixTags.MSG_TYPE, messageType);
    }

    public Optional<String> get(int tag) {
        return Optional.ofNullable(fields.get(tag));
    }

    public String require(int tag) {
        return get(tag).orElseThrow(() -> new FixValidationException("Missing required tag " + tag));
    }

    public String messageType() {
        return require(FixTags.MSG_TYPE);
    }

    public int sequenceNumber() {
        String value = require(FixTags.MSG_SEQ_NUM);
        try {
            int sequenceNumber = Integer.parseInt(value);
            if (sequenceNumber <= 0) {
                throw new NumberFormatException("non-positive");
            }
            return sequenceNumber;
        } catch (NumberFormatException ex) {
            throw new FixValidationException("Invalid MsgSeqNum: " + value, ex);
        }
    }

    public Map<Integer, String> fields() {
        return Collections.unmodifiableMap(fields);
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        fields.forEach(builder::put);
        return builder;
    }

    @Override
    public String toString() {
        return "FixMessage" + fields;
    }

    public static final class Builder {
        private final LinkedHashMap<Integer, String> fields = new LinkedHashMap<>();

        public Builder put(int tag, Object value) {
            if (value != null) {
                fields.put(tag, String.valueOf(value));
            }
            return this;
        }

        public FixMessage build() {
            return new FixMessage(fields);
        }
    }
}

