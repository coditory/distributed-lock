package com.coditory.sherlock;

import java.util.UUID;

/**
 * Random unique id generator
 */
final class UuidGenerator {
    private UuidGenerator() {
        throw new IllegalStateException("Do not instantiate utility class");
    }

    /**
     * @return random unique id
     */
    public static String uuid() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}
