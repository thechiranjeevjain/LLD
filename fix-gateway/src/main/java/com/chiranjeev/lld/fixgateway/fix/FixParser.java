package com.chiranjeev.lld.fixgateway.fix;

import com.chiranjeev.lld.fixgateway.gateway.FixParseException;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public final class FixParser {
    public FixMessage parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new FixParseException("FIX message is empty");
        }

        String normalized = normalizeDelimiter(rawMessage);
        validateBodyLengthIfPresent(normalized);
        validateChecksumIfPresent(normalized);

        LinkedHashMap<Integer, String> fields = new LinkedHashMap<>();
        String[] tokens = normalized.split(Pattern.quote(String.valueOf(FixSerializer.SOH)));
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }

            int separator = token.indexOf('=');
            if (separator <= 0) {
                throw new FixParseException("Invalid FIX field: " + token);
            }

            String tagValue = token.substring(0, separator);
            try {
                int tag = Integer.parseInt(tagValue);
                String value = token.substring(separator + 1);
                fields.put(tag, value);
            } catch (NumberFormatException ex) {
                throw new FixParseException("Invalid FIX tag: " + tagValue, ex);
            }
        }

        if (fields.isEmpty()) {
            throw new FixParseException("No fields found in FIX message");
        }

        return new FixMessage(fields);
    }

    private String normalizeDelimiter(String rawMessage) {
        if (rawMessage.indexOf(FixSerializer.SOH) >= 0) {
            return rawMessage.endsWith(String.valueOf(FixSerializer.SOH))
                    ? rawMessage
                    : rawMessage + FixSerializer.SOH;
        }
        String pipeNormalized = rawMessage.replace('|', FixSerializer.SOH);
        return pipeNormalized.endsWith(String.valueOf(FixSerializer.SOH))
                ? pipeNormalized
                : pipeNormalized + FixSerializer.SOH;
    }

    private void validateBodyLengthIfPresent(String message) {
        String bodyLength = findTagValue(message, FixTags.BODY_LENGTH);
        if (bodyLength == null) {
            return;
        }

        int expectedLength;
        try {
            expectedLength = Integer.parseInt(bodyLength);
        } catch (NumberFormatException ex) {
            throw new FixParseException("Invalid BodyLength: " + bodyLength, ex);
        }

        int tag9Start = message.indexOf(FixSerializer.SOH + "9=");
        if (tag9Start < 0) {
            return;
        }

        int bodyStart = message.indexOf(FixSerializer.SOH, tag9Start + 1) + 1;
        int checksumStart = message.lastIndexOf(FixSerializer.SOH + "10=");
        if (bodyStart <= 0 || checksumStart < bodyStart) {
            return;
        }

        int actualLength = message.substring(bodyStart, checksumStart + 1)
                .getBytes(StandardCharsets.US_ASCII)
                .length;
        if (actualLength != expectedLength) {
            throw new FixParseException("BodyLength mismatch. expected=" + expectedLength + ", actual=" + actualLength);
        }
    }

    private void validateChecksumIfPresent(String message) {
        String expected = findTagValue(message, FixTags.CHECK_SUM);
        if (expected == null) {
            return;
        }

        if (!expected.matches("\\d{3}")) {
            throw new FixParseException("Invalid CheckSum format: " + expected);
        }

        int checksumStart = message.lastIndexOf(FixSerializer.SOH + "10=");
        if (checksumStart < 0) {
            throw new FixParseException("CheckSum tag must be the final tag");
        }

        String checksumData = message.substring(0, checksumStart + 1);
        String actual = Checksum.format(Checksum.calculate(checksumData));
        if (!actual.equals(expected)) {
            throw new FixParseException("CheckSum mismatch. expected=" + expected + ", actual=" + actual);
        }
    }

    private String findTagValue(String message, int tag) {
        String prefix = tag + "=";
        String middlePrefix = FixSerializer.SOH + prefix;

        int start = message.startsWith(prefix) ? 0 : message.indexOf(middlePrefix);
        if (start < 0) {
            return null;
        }
        if (start > 0) {
            start++;
        }
        int valueStart = start + prefix.length();
        int valueEnd = message.indexOf(FixSerializer.SOH, valueStart);
        if (valueEnd < 0) {
            valueEnd = message.length();
        }
        return message.substring(valueStart, valueEnd);
    }
}

