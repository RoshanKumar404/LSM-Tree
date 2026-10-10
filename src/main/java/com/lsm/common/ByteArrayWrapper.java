package com.lsm.common;

import java.util.Arrays;
import java.util.Objects;

public final class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {
    private final byte[] bytes;

    private ByteArrayWrapper(byte[] bytes) {
        this.bytes = bytes;
    }

    public static ByteArrayWrapper copyOf(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be empty");
        }
        return new ByteArrayWrapper(Arrays.copyOf(bytes, bytes.length));
    }

    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public int compareTo(ByteArrayWrapper other) {
        int minLength = Math.min(bytes.length, other.bytes.length);
        for (int i = 0; i < minLength; i++) {
            int left = Byte.toUnsignedInt(bytes[i]);
            int right = Byte.toUnsignedInt(other.bytes[i]);
            if (left != right) {
                return Integer.compare(left, right);
            }
        }
        return Integer.compare(bytes.length, other.bytes.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ByteArrayWrapper other)) {
            return false;
        }
        return Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }
}
