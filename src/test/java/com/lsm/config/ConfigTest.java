package com.lsm.config;

import com.lsm.wal.WalSyncMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Config -- covers defaults, fluent setters, and validation guards.
 */
class ConfigTest {

    // --- Defaults ---

    @Test
    void defaultMemTableSizeIs32MB() {
        Config config = new Config();
        assertEquals(32L * 1024L * 1024L, config.memTableSizeBytes());
    }

    @Test
    void defaultWalSyncModeIsAlways() {
        // ALWAYS is the safest default -- no data loss on crash
        Config config = new Config();
        assertEquals(WalSyncMode.ALWAYS, config.walSyncMode());
    }

    @Test
    void defaultDataDirectoryIsNull() {
        // null means KVEngine.open() will use dbPath as the data directory
        Config config = new Config();
        assertNull(config.dataDirectory());
    }

    // --- MemTable setters ---

    @Test
    void setMemTableSizeBytesUpdatesValue() {
        Config config = new Config().setMemTableSizeBytes(64 * 1024 * 1024L);
        assertEquals(64 * 1024 * 1024L, config.memTableSizeBytes());
    }

    @Test
    void setMemTableSizeMBConvertsToBytes() {
        Config config = new Config().setMemTableSizeMB(16);
        assertEquals(16L * 1024L * 1024L, config.memTableSizeBytes());
    }

    @Test
    void setMemTableSizeBytesRejectsZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Config().setMemTableSizeBytes(0));
    }

    @Test
    void setMemTableSizeBytesRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new Config().setMemTableSizeBytes(-1));
    }

    @Test
    void setMemTableSizeMBRejectsZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Config().setMemTableSizeMB(0));
    }

    // --- WAL sync mode setter ---

    @Test
    void setWalSyncModeUpdatesValue() {
        Config config = new Config().setWalSyncMode(WalSyncMode.OS);
        assertEquals(WalSyncMode.OS, config.walSyncMode());
    }

    @Test
    void setWalSyncModeToNone() {
        Config config = new Config().setWalSyncMode(WalSyncMode.NONE);
        assertEquals(WalSyncMode.NONE, config.walSyncMode());
    }

    @Test
    void setWalSyncModeRejectsNull() {
        assertThrows(NullPointerException.class,
            () -> new Config().setWalSyncMode(null));
    }

    // --- Data directory setter ---

    @Test
    void setDataDirectoryUpdatesValue() {
        Path dir = Paths.get("/data/lsm");
        Config config = new Config().setDataDirectory(dir);
        assertEquals(dir, config.dataDirectory());
    }

    @Test
    void setDataDirectoryRejectsNull() {
        assertThrows(NullPointerException.class,
            () -> new Config().setDataDirectory(null));
    }

    // --- Fluent chaining ---

    @Test
    void fluentChainingWorksAcrossAllSetters() {
        Path dir = Paths.get("/tmp/lsm-data");

        Config config = new Config()
            .setMemTableSizeMB(64)
            .setWalSyncMode(WalSyncMode.OS)
            .setDataDirectory(dir);

        assertEquals(64L * 1024L * 1024L, config.memTableSizeBytes());
        assertEquals(WalSyncMode.OS, config.walSyncMode());
        assertEquals(dir, config.dataDirectory());
    }
}
