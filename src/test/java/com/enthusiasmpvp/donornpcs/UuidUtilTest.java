package com.enthusiasmpvp.donornpcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class UuidUtilTest {
    private static final UUID UUID_VALUE = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void parsesDashedAndUndashedUuidForms() {
        assertEquals(UUID_VALUE, UuidUtil.parseUuid(UUID_VALUE.toString()).orElseThrow());
        assertEquals(UUID_VALUE, UuidUtil.parseUuid("123e4567e89b12d3a456426614174000").orElseThrow());
    }

    @Test
    void trimsInputBeforeParsing() {
        assertEquals(UUID_VALUE, UuidUtil.parseUuid("  " + UUID_VALUE + "  ").orElseThrow());
    }

    @Test
    void rejectsNullBlankMalformedAndWrongLengthValues() {
        assertTrue(UuidUtil.parseUuid(null).isEmpty());
        assertTrue(UuidUtil.parseUuid("").isEmpty());
        assertTrue(UuidUtil.parseUuid("not-a-uuid").isEmpty());
        assertTrue(UuidUtil.parseUuid("123e4567e89b12d3a45642661417400").isEmpty());
    }

    @Test
    void undashedRoundTripsThroughParser() {
        String compact = UuidUtil.undashed(UUID_VALUE);
        assertEquals(32, compact.length());
        assertEquals(UUID_VALUE, UuidUtil.parseUuid(compact).orElseThrow());
    }
}
