package com.lsm.record;

import java.util.Arrays;
import java.util.Objects;

public final class Record {
    private static final int FIXED_METADATA_BYTES = Byte.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES;

    private final RecordType type;
    private final byte[] key;
    private final byte[] value;
    private final long sequenceNumber;

    private Record(RecordType type, byte[] key, byte[] value, long sequenceNumber) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.key = copyRequiredKey(key);
        this.value = value == null ? null : Arrays.copyOf(value, value.length);
        this.sequenceNumber = sequenceNumber;
    }

    public static Record put(byte[] key, byte[] value, long sequenceNumber) {
        Objects.requireNonNull(value, "value must not be null");
        return new Record(RecordType.PUT, key, value, sequenceNumber);
    }

    public static Record delete(byte[] key, long sequenceNumber) {
        return new Record(RecordType.DELETE, key, null, sequenceNumber);
    }

    public RecordType type() {
        return type;
    }

    public byte[] key() {
        return Arrays.copyOf(key, key.length);
    }

    public byte[] value() {
        if (value == null) {
            return null;
        }
        return Arrays.copyOf(value, value.length);
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public boolean isTombstone() {
        return type == RecordType.DELETE;
    }

    public long estimatedSizeBytes() {
        int valueLength = value == null ? 0 : value.length;
        return FIXED_METADATA_BYTES + key.length + valueLength;
    }

    private static byte[] copyRequiredKey(byte[] key) {
        Objects.requireNonNull(key, "key must not be null");
        if (key.length == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
        return Arrays.copyOf(key, key.length);
    }
}
