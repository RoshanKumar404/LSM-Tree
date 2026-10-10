package com.lsm.api;

import com.lsm.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KVEngineTest {
    @TempDir
    Path tempDir;

    @Test
    void opensDatabaseDirectoryAndSupportsPutGetDelete() {
        Path dbPath = tempDir.resolve("db");

        try (KVEngine engine = KVEngine.open(dbPath, new Config())) {
            engine.put(bytes("user:1"), bytes("Roshan"));

            assertTrue(Files.isDirectory(dbPath));
            assertArrayEquals(bytes("Roshan"), engine.get(bytes("user:1")));

            engine.delete(bytes("user:1"));

            assertNull(engine.get(bytes("user:1")));
        }
    }

    @Test
    void rejectsOperationsAfterClose() {
        KVEngine engine = KVEngine.open(tempDir.resolve("db"), new Config());
        engine.close();

        assertThrows(IllegalStateException.class, () -> engine.put(bytes("k"), bytes("v")));
        assertThrows(IllegalStateException.class, () -> engine.get(bytes("k")));
        assertThrows(IllegalStateException.class, () -> engine.delete(bytes("k")));
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
