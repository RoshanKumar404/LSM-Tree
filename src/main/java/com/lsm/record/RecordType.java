package com.lsm.record;

public enum RecordType {
    PUT((byte) 1),
    DELETE((byte) 2);

    private final byte code;

    RecordType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static RecordType fromCode(byte code) {
        for (RecordType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown record type code: " + code);
    }
}
