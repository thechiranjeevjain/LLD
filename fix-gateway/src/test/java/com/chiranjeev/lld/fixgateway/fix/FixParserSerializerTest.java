package com.chiranjeev.lld.fixgateway.fix;

import com.chiranjeev.lld.fixgateway.gateway.FixParseException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixParserSerializerTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void serializesWithBodyLengthAndChecksum() {
        FixSerializer serializer = new FixSerializer(FIXED_CLOCK);
        String raw = serializer.serialize(
                new SessionEndpoint("CLIENT1", "FIX-GW"),
                1,
                FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                        .put(FixTags.ENCRYPT_METHOD, "0")
                        .put(FixTags.HEART_BT_INT, "30")
                        .build()
        );

        String printable = serializer.toPrintable(raw);
        assertTrue(printable.startsWith("8=FIX.4.4|9=68|35=A|34=1|49=CLIENT1|56=FIX-GW|52=20260805-12:00:00.000|"));
        assertTrue(printable.endsWith("|10=144|"));
    }

    @Test
    void parsesPipeDelimitedMessages() {
        FixMessage message = new FixParser().parse(
                "8=FIX.4.4|9=68|35=A|34=1|49=CLIENT1|56=FIX-GW|52=20260805-12:00:00.000|98=0|108=30|10=144|"
        );

        assertEquals(FixTags.MSG_TYPE_LOGON, message.require(FixTags.MSG_TYPE));
        assertEquals("CLIENT1", message.require(FixTags.SENDER_COMP_ID));
        assertEquals(1, message.sequenceNumber());
    }

    @Test
    void rejectsTamperedChecksum() {
        FixParser parser = new FixParser();
        assertThrows(
                FixParseException.class,
                () -> parser.parse("8=FIX.4.4|9=68|35=A|34=1|49=CLIENT1|56=FIX-GW|52=20260805-12:00:00.000|98=0|108=30|10=000|")
        );
    }
}
