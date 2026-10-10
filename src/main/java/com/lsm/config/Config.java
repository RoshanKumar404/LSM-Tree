package com.lsm.config;

public final class Config {
    public static final long DEFAULT_MEM_TABLE_SIZE_BYTES = 32L * 1024L * 1024L;

    private long memTableSizeBytes = DEFAULT_MEM_TABLE_SIZE_BYTES;

    public long memTableSizeBytes() {
        return memTableSizeBytes;
    }

    public Config setMemTableSizeBytes(long memTableSizeBytes) {
        if (memTableSizeBytes <= 0) {
            throw new IllegalArgumentException("memTableSizeBytes must be positive");
        }
        this.memTableSizeBytes = memTableSizeBytes;
        return this;
    }

    public Config setMemTableSizeMB(long memTableSizeMB) {
        if (memTableSizeMB <= 0) {
            throw new IllegalArgumentException("memTableSizeMB must be positive");
        }
        return setMemTableSizeBytes(memTableSizeMB * 1024L * 1024L);
    }
}
