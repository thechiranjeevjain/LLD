package com.chiranjeev.lld.fixgateway.fix;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public final class FixSerializer {
    public static final char SOH = '\u0001';
    private static final String BEGIN_STRING = "FIX.4.4";
    private static final DateTimeFormatter SENDING_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    private static final Set<Integer> SESSION_HEADER_TAGS = Set.of(
            FixTags.BEGIN_STRING,
            FixTags.BODY_LENGTH,
            FixTags.MSG_TYPE,
            FixTags.MSG_SEQ_NUM,
            FixTags.SENDER_COMP_ID,
            FixTags.TARGET_COMP_ID,
            FixTags.SENDING_TIME,
            FixTags.CHECK_SUM
    );

    private final Clock clock;

    public FixSerializer() {
        this(Clock.systemUTC());
    }

    public FixSerializer(Clock clock) {
        this.clock = clock;
    }

    public String serialize(SessionEndpoint endpoint, int sequenceNumber, FixMessage message) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("sequenceNumber must be positive");
        }

        StringBuilder body = new StringBuilder();
        appendField(body, FixTags.MSG_TYPE, message.messageType());
        appendField(body, FixTags.MSG_SEQ_NUM, sequenceNumber);
        appendField(body, FixTags.SENDER_COMP_ID, endpoint.senderCompId());
        appendField(body, FixTags.TARGET_COMP_ID, endpoint.targetCompId());
        appendField(body, FixTags.SENDING_TIME, SENDING_TIME_FORMATTER.format(Instant.now(clock)));

        message.fields().forEach((tag, value) -> {
            if (!SESSION_HEADER_TAGS.contains(tag)) {
                appendField(body, tag, value);
            }
        });

        int bodyLength = body.toString().getBytes(StandardCharsets.US_ASCII).length;
        String headerAndBody = "8=" + BEGIN_STRING + SOH + "9=" + bodyLength + SOH + body;
        String checksum = Checksum.format(Checksum.calculate(headerAndBody));
        return headerAndBody + "10=" + checksum + SOH;
    }

    public String toPrintable(String fixMessage) {
        return fixMessage.replace(SOH, '|');
    }

    private static void appendField(StringBuilder builder, int tag, Object value) {
        builder.append(tag).append('=').append(value).append(SOH);
    }
}

