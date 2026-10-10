package com.lsm.api;

import com.lsm.config.Config;
import com.lsm.memtable.MemTable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class KVEngine implements AutoCloseable {
    private final Path dbPath;
    private final Config config;
    private final MemTable memTable;
    private final AtomicLong sequenceNumber;
    private volatile boolean closed;

    private KVEngine(Path dbPath, Config config) {
        this.dbPath = dbPath;
        this.config = config;
        this.memTable = new MemTable();
        this.sequenceNumber = new AtomicLong();
    }

    public static KVEngine open(Path dbPath, Config config) {
        Objects.requireNonNull(dbPath, "dbPath must not be null");
        Objects.requireNonNull(config, "config must not be null");
        try {
            Files.createDirectories(dbPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create database directory: " + dbPath, e);
        }
        return new KVEngine(dbPath, config);
    }

    public synchronized void put(byte[] key, byte[] value) {
        ensureOpen();
        memTable.put(key, value, sequenceNumber.incrementAndGet());
    }

    public byte[] get(byte[] key) {
        ensureOpen();
        return memTable.get(key);
    }

    public synchronized void delete(byte[] key) {
        ensureOpen();
        memTable.delete(key, sequenceNumber.incrementAndGet());
    }

    public Path dbPath() {
        return dbPath;
    }

    public Config config() {
        return config;
    }

    public MemTable memTable() {
        return memTable;
    }

    @Override
    public synchronized void close() {
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("KVEngine is closed");
        }
    }
}
