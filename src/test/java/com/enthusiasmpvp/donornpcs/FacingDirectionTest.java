package com.enthusiasmpvp.donornpcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class FacingDirectionTest {
    @Test
    void parsesEveryDirectionCaseInsensitively() {
        assertEquals(FacingDirection.NORTH, FacingDirection.fromConfig("north"));
        assertEquals(FacingDirection.EAST, FacingDirection.fromConfig(" EAST "));
        assertEquals(FacingDirection.SOUTH, FacingDirection.fromConfig("South"));
        assertEquals(FacingDirection.WEST, FacingDirection.fromConfig("west"));
    }

    @Test
    void missingOrInvalidConfigurationFallsBackToEast() {
        assertEquals(FacingDirection.EAST, FacingDirection.fromConfig(null));
        assertEquals(FacingDirection.EAST, FacingDirection.fromConfig(""));
        assertEquals(FacingDirection.EAST, FacingDirection.fromConfig("diagonal"));
    }

    @Test
    void directionGeometryMatchesNpcFacingContract() {
        assertEquals(180.0F, FacingDirection.NORTH.yaw());
        assertEquals(-90.0F, FacingDirection.EAST.yaw());
        assertEquals(0.0F, FacingDirection.SOUTH.yaw());
        assertEquals(90.0F, FacingDirection.WEST.yaw());

        assertEquals(-10.0, FacingDirection.NORTH.targetZOffset());
        assertEquals(10.0, FacingDirection.EAST.targetXOffset());
        assertEquals(10.0, FacingDirection.SOUTH.targetZOffset());
        assertEquals(-10.0, FacingDirection.WEST.targetXOffset());
        assertEquals(0.0, FacingDirection.NORTH.targetYOffset());
    }
}
