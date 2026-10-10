package com.lsm.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteArrayWrapperTest {
    @Test
    void ordersBytesUsingUnsignedLexicographicComparison() {
        List<ByteArrayWrapper> keys = new ArrayList<>();
        keys.add(ByteArrayWrapper.copyOf(new byte[]{(byte) 0xFF}));
        keys.add(ByteArrayWrapper.copyOf(new byte[]{0x01}));
        keys.add(ByteArrayWrapper.copyOf(new byte[]{0x01, 0x02}));

        keys.sort(ByteArrayWrapper::compareTo);

        assertEquals(ByteArrayWrapper.copyOf(new byte[]{0x01}), keys.get(0));
        assertEquals(ByteArrayWrapper.copyOf(new byte[]{0x01, 0x02}), keys.get(1));
        assertEquals(ByteArrayWrapper.copyOf(new byte[]{(byte) 0xFF}), keys.get(2));
    }

    @Test
    void rejectsEmptyKeys() {
        assertThrows(IllegalArgumentException.class, () -> ByteArrayWrapper.copyOf(new byte[0]));
    }
}
