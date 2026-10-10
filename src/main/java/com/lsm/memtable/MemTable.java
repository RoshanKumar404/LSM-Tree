package com.lsm.memtable;

import com.lsm.common.ByteArrayWrapper;
import com.lsm.record.Record;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public final class MemTable {
    private final ConcurrentSkipListMap<ByteArrayWrapper, Record> records = new ConcurrentSkipListMap<>();
    private final AtomicLong estimatedSizeBytes = new AtomicLong();

    public void put(byte[] key, byte[] value, long sequenceNumber) {
        putRecord(Record.put(key, value, sequenceNumber));
    }

    public void delete(byte[] key, long sequenceNumber) {
        putRecord(Record.delete(key, sequenceNumber));
    }

    public void putRecord(Record record) {
        ByteArrayWrapper wrappedKey = ByteArrayWrapper.copyOf(record.key());
        Record previous = records.put(wrappedKey, record);

        long previousSize = previous == null ? 0 : previous.estimatedSizeBytes();
        estimatedSizeBytes.addAndGet(record.estimatedSizeBytes() - previousSize);
    }

    public Optional<Record> getRecord(byte[] key) {
        return Optional.ofNullable(records.get(ByteArrayWrapper.copyOf(key)));
    }

    public byte[] get(byte[] key) {
        Record record = records.get(ByteArrayWrapper.copyOf(key));
        if (record == null || record.isTombstone()) {
            return null;
        }
        return record.value();
    }

    public Collection<Record> recordsInOrder() {
        return List.copyOf(records.values());
    }

    public int size() {
        return records.size();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public long estimatedSizeBytes() {
        return estimatedSizeBytes.get();
    }
}
