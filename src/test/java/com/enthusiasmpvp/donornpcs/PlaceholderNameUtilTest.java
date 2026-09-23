package com.enthusiasmpvp.donornpcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PlaceholderNameUtilTest {
    @Test
    void rejectsMissingSentinelAndPlaceholderEchoValues() {
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", null));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "   "));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "%player_name%"));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", " null "));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "NONE"));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "N/A"));
        assertTrue(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "-"));
    }

    @Test
    void acceptsAndTrimsRealNames() {
        assertFalse(PlaceholderNameUtil.isInvalidPlaceholderValue("%player_name%", "  P2wn  "));
        assertEquals("P2wn", PlaceholderNameUtil.cleanOrDefault("%player_name%", "  P2wn  ", "Steve"));
    }

    @Test
    void defaultNameIsUsedOnlyForInvalidValues() {
        assertEquals("Steve", PlaceholderNameUtil.cleanOrDefault("%player_name%", null, "Steve"));
        assertEquals("Steve", PlaceholderNameUtil.cleanOrDefault("%player_name%", "%player_name%", "Steve"));
        assertEquals("Alex", PlaceholderNameUtil.cleanOrDefault("%player_name%", "Alex", "Steve"));
    }

    @Test
    void uuidCleaningUsesEmptyStringForInvalidPlaceholderOutput() {
        assertEquals("", PlaceholderNameUtil.cleanUuidValue("%player_uuid%", null));
        assertEquals("", PlaceholderNameUtil.cleanUuidValue("%player_uuid%", "%player_uuid%"));
        assertEquals("1234", PlaceholderNameUtil.cleanUuidValue("%player_uuid%", " 1234 "));
    }
}
