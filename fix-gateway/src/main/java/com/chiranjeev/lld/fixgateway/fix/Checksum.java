package com.chiranjeev.lld.fixgateway.fix;

import java.nio.charset.StandardCharsets;

public final class Checksum {
    private Checksum() {
    }

    public static int calculate(CharSequence messageWithoutChecksum) {
        byte[] bytes = messageWithoutChecksum.toString().getBytes(StandardCharsets.US_ASCII);
        int sum = 0;
        for (byte value : bytes) {
            sum += value & 0xFF;
        }
        return sum % 256;
    }

    public static String format(int checksum) {
        if (checksum < 0 || checksum > 255) {
            throw new IllegalArgumentException("checksum must be between 0 and 255");
        }
        return String.format("%03d", checksum);
    }
}

