package com.lsm.wal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for WalSyncMode enum.
 *
 * There are only 3 meaningful things to verify for an enum:
 * 1. All expected values exist (no accidental deletion or renaming)
 * 2. The declared order is stable (code may rely on ordinal in tests)
 * 3. The values() array has no unexpected extras
 */
class WalSyncModeTest {

    @Test
    void allThreeModesExist() {
        // Verify each constant is accessible by name
        assertNotNull(WalSyncMode.ALWAYS);
        assertNotNull(WalSyncMode.OS);
        assertNotNull(WalSyncMode.NONE);
    }

    @Test
    void exactlyThreeModesDefined() {
        // Guard against accidental additions that callers might not handle
        assertEquals(3, WalSyncMode.values().length,
            "WalSyncMode must have exactly 3 values: ALWAYS, OS, NONE");
    }

    @Test
    void valueOfRoundtrips() {
        // Verify Enum.valueOf() works — important for config serialization
        assertEquals(WalSyncMode.ALWAYS, WalSyncMode.valueOf("ALWAYS"));
        assertEquals(WalSyncMode.OS,     WalSyncMode.valueOf("OS"));
        assertEquals(WalSyncMode.NONE,   WalSyncMode.valueOf("NONE"));
    }
}
