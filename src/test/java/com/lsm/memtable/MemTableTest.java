package com.lsm.memtable;

import com.lsm.record.Record;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemTableTest {
    @Test
    void storesAndReadsValue() {
        MemTable memTable = new MemTable();

        memTable.put(bytes("user:1"), bytes("Roshan"), 1);

        assertArrayEquals(bytes("Roshan"), memTable.get(bytes("user:1")));
    }

    @Test
    void overwritesExistingValue() {
        MemTable memTable = new MemTable();

        memTable.put(bytes("user:1"), bytes("old"), 1);
        memTable.put(bytes("user:1"), bytes("new"), 2);

        assertArrayEquals(bytes("new"), memTable.get(bytes("user:1")));
        assertEquals(1, memTable.size());
    }

    @Test
    void deleteWritesTombstoneAndHidesValue() {
        MemTable memTable = new MemTable();

        memTable.put(bytes("session:1"), bytes("active"), 1);
        memTable.delete(bytes("session:1"), 2);

        assertNull(memTable.get(bytes("session:1")));
        assertTrue(memTable.getRecord(bytes("session:1")).orElseThrow().isTombstone());
    }

    @Test
    void iteratesInSortedKeyOrder() {
        MemTable memTable = new MemTable();

        memTable.put(bytes("c"), bytes("3"), 1);
        memTable.put(bytes("a"), bytes("1"), 2);
        memTable.put(bytes("b"), bytes("2"), 3);

        List<String> keys = memTable.recordsInOrder().stream()
                .map(Record::key)
                .map(MemTableTest::string)
                .toList();

        assertEquals(List.of("a", "b", "c"), keys);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static String string(byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }
}
